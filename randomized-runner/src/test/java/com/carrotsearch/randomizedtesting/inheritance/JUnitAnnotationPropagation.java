package com.carrotsearch.randomizedtesting.inheritance;

import java.util.Collections;
import java.util.List;

import org.fest.assertions.api.Assertions;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runners.model.Statement;

import com.carrotsearch.randomizedtesting.RandomizedRunner;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class JUnitAnnotationPropagation {
  final static List<String> order = Lists.newArrayList();

  public static class Super {
    @Rule
    public TestRule rules = new TestRule() {
      @Override
      public Statement apply(final Statement base, Description description) {
        return new Statement() {
          public void evaluate() throws Throwable {
            order.add("rule-before");
            base.evaluate();
            order.add("rule-after");
          }
        };
      }
    };

    @BeforeClass public static void beforeClass1() { order.add("super.beforeclass1"); }
                 public static void beforeClass2() { order.add("super.beforeclass2"); }
    @BeforeClass public static void beforeClass3() { order.add("super.beforeclass3"); }

    @Before      public void before1() { order.add("super.before1"); }
                 public void before2() { order.add("super.before2"); }
    @Before      public void before3() { order.add("super.before3"); }

    @Test        public void testMethod1() { order.add("super.testMethod1"); }
                 public void testMethod2() { order.add("super.testMethod2"); }
    @Test        public void testMethod3() { order.add("super.testMethod3"); }

    // Awkward cases of annotations and virtual methods.
    @Ignore      public void testMethod4() { order.add("super.testMethod4"); }
    @Test        public void testMethod5() { order.add("super.testMethod5"); }
  }

  public static class Sub extends Super {
                 public static void beforeClass1() { order.add("sub.beforeclass1"); }
    @BeforeClass public static void beforeClass2() { order.add("sub.beforeclass2"); }
    @BeforeClass public static void beforeClass3() { order.add("sub.beforeclass3"); }

                 public void before1() { order.add("sub.before1"); }
    @Before      public void before2() { order.add("sub.before2"); }
    @Before      public void before3() { order.add("sub.before3"); }

                 public void testMethod1() { order.add("sub.testMethod1"); }
    @Test        public void testMethod2() { order.add("sub.testMethod2"); }
    @Test        public void testMethod3() { order.add("sub.testMethod3"); }

    @Test        public void testMethod4() { order.add("sub.testMethod4"); }
    @Ignore      public void testMethod5() { order.add("sub.testMethod5"); }
  }

  @Test
  public void checkOldMethodRules() throws Exception {
    assertSameExecution(Sub.class);
  }

  private void assertSameExecution(Class<?> clazz) throws Exception {
    order.clear();
    JUnitCore.runClasses(clazz);
    List<String> order1 = Lists.newArrayList(order);
    order.clear();

    new JUnitCore().run(Request.runner(new RandomizedRunner(clazz)));
    List<String> order2 = Lists.newArrayList(order);
    order.clear();

    String msg = "# JUnit order:\n" + Joiner.on("\n").join(order1) + "\n" +
                 "# RR order:\n" + Joiner.on("\n").join(order2);

    // Don't care about relative ordering of hook methods.
    Assertions.assertThat(noNumbers(order2)).as(msg).isEqualTo(noNumbers(order1));

    // But do care that all methods show up.
    Collections.sort(order1);
    Collections.sort(order2);
    Assertions.assertThat(order2).as(msg).isEqualTo(order1);
  }

  private List<String> noNumbers(List<String> in) {
    List<String> out = Lists.newArrayList();
    for (String s : in) {
      out.add(s.replaceAll("[0-9]+$", ""));
    }
    return out;
  }
}
