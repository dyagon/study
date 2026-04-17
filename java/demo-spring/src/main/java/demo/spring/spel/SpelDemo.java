package demo.spring.spel;

import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.List;
import java.util.Map;

/**
 * SpEL (Spring Expression Language) 主要功能演示。
 * 演示：字面量、属性/方法、类型 T()、变量、集合选择/投影、三元与 Elvis、安全导航、正则等。
 */
public final class SpelDemo {

    private static final ExpressionParser PARSER = new SpelExpressionParser();

    /** 运行所有演示（可从 main 或 Spring CommandLineRunner 调用） */
    public static void runAll() {
        literalAndProperty();
        typeOperator();
        variablesAndRoot();
        collectionSelectionAndProjection();
        ternaryAndElvis();
        safeNavigation();
        regexAndOperators();
        listAndMapAccess();
    }

    public static void main(String[] args) {
        runAll();
    }

    /** 1. 字面量与属性/方法访问 */
    private static void literalAndProperty() {
        printSection("1. 字面量与属性/方法访问");

        Expression e1 = PARSER.parseExpression("'Hello SpEL'");
        System.out.println("  'Hello SpEL' => " + e1.getValue(String.class));

        Expression e2 = PARSER.parseExpression("42");
        System.out.println("  42 => " + e2.getValue(Integer.class));

        Expression e3 = PARSER.parseExpression("'hello'.toUpperCase()");
        System.out.println("  'hello'.toUpperCase() => " + e3.getValue(String.class));

        Expression e4 = PARSER.parseExpression("'abc'.bytes.length");
        System.out.println("  'abc'.bytes.length => " + e4.getValue(Integer.class));

        Expression e5 = PARSER.parseExpression("new String('world')");
        System.out.println("  new String('world') => " + e5.getValue(String.class));
    }

    /** 2. 类型运算符 T()：调用静态方法/字段 */
    private static void typeOperator() {
        printSection("2. 类型运算符 T()");

        Expression e1 = PARSER.parseExpression("T(java.lang.Math).PI");
        System.out.println("  T(java.lang.Math).PI => " + e1.getValue(Double.class));

        Expression e2 = PARSER.parseExpression("T(java.lang.Math).random()");
        System.out.println("  T(java.lang.Math).random() => " + e2.getValue(Double.class));

        Expression e3 = PARSER.parseExpression("T(java.lang.System).getProperty('user.name')");
        System.out.println("  T(java.lang.System).getProperty('user.name') => " + e3.getValue(String.class));
    }

    /** 3. 变量 #var、#root、#this */
    private static void variablesAndRoot() {
        printSection("3. 变量 #var、#root、#this");

        StandardEvaluationContext ctx = new StandardEvaluationContext();
        ctx.setVariable("name", "SpEL");
        ctx.setVariable("score", 95);
        ctx.setRootObject(new Person("张三", 28));

        Expression e1 = PARSER.parseExpression("#name");
        System.out.println("  #name => " + e1.getValue(ctx, String.class));

        Expression e2 = PARSER.parseExpression("#score + 5");
        System.out.println("  #score + 5 => " + e2.getValue(ctx, Integer.class));

        Expression e3 = PARSER.parseExpression("#root.name");
        System.out.println("  #root.name (根对象属性) => " + e3.getValue(ctx, String.class));

        Expression e4 = PARSER.parseExpression("#root.age");
        System.out.println("  #root.age => " + e4.getValue(ctx, Integer.class));
    }

    /** 4. 集合选择 ?[] 与投影 ![] */
    private static void collectionSelectionAndProjection() {
        printSection("4. 集合选择 ?[] 与投影 ![]");

        StandardEvaluationContext ctx = new StandardEvaluationContext();
        ctx.setVariable("nums", List.of(1, 2, 3, 4, 5, 6));

        // 选择：满足条件的元素
        Expression select = PARSER.parseExpression("#nums.?[#this > 3]");
        @SuppressWarnings("unchecked")
        List<Integer> selected = (List<Integer>) select.getValue(ctx);
        System.out.println("  #nums.?[#this > 3] (选择>3) => " + selected);

        // 投影：对每个元素做变换
        Expression project = PARSER.parseExpression("#nums.![#this * 2]");
        @SuppressWarnings("unchecked")
        List<Integer> projected = (List<Integer>) project.getValue(ctx);
        System.out.println("  #nums.![#this * 2] (每项乘2) => " + projected);

        ctx.setVariable("names", List.of("a", "ab", "abc"));
        Expression first = PARSER.parseExpression("#names.^[#this.length() > 1]");
        System.out.println("  #names.^[#this.length() > 1] (第一个长度>1) => " + first.getValue(ctx));
        Expression last = PARSER.parseExpression("#names.$[#this.length() > 1]");
        System.out.println("  #names.$[#this.length() > 1] (最后一个长度>1) => " + last.getValue(ctx));
    }

    /** 5. 三元运算符与 Elvis 运算符 ?: */
    private static void ternaryAndElvis() {
        printSection("5. 三元与 Elvis ?:");

        Expression ternary = PARSER.parseExpression("true ? 'yes' : 'no'");
        System.out.println("  true ? 'yes' : 'no' => " + ternary.getValue(String.class));

        StandardEvaluationContext ctx = new StandardEvaluationContext();
        ctx.setVariable("nick", null);
        Expression elvis = PARSER.parseExpression("#nick ?: 'default'");
        System.out.println("  #nick ?: 'default' (Elvis, nick=null) => " + elvis.getValue(ctx, String.class));

        ctx.setVariable("nick", "SpEL");
        System.out.println("  #nick ?: 'default' (Elvis, nick=SpEL) => " + elvis.getValue(ctx, String.class));
    }

    /** 6. 安全导航 ?. 避免 NPE */
    private static void safeNavigation() {
        printSection("6. 安全导航 ?.");

        StandardEvaluationContext ctx = new StandardEvaluationContext();
        ctx.setVariable("person", new Person("李四", 30));
        Expression safe = PARSER.parseExpression("#person?.name");
        System.out.println("  #person?.name (person 非 null) => " + safe.getValue(ctx, String.class));

        ctx.setVariable("person", null);
        System.out.println("  #person?.name (person=null) => " + safe.getValue(ctx));
    }

    /** 7. 正则 matches、比较与逻辑 */
    private static void regexAndOperators() {
        printSection("7. 正则 matches、比较与逻辑");

        Expression matches = PARSER.parseExpression("'hello123'.matches('[a-z]+[0-9]+')");
        System.out.println("  'hello123'.matches('[a-z]+[0-9]+') => " + matches.getValue(Boolean.class));

        Expression and = PARSER.parseExpression("true and false");
        System.out.println("  true and false => " + and.getValue(Boolean.class));

        Expression or = PARSER.parseExpression("true or false");
        System.out.println("  true or false => " + or.getValue(Boolean.class));

        Expression not = PARSER.parseExpression("!false");
        System.out.println("  !false => " + not.getValue(Boolean.class));
    }

    /** 8. 列表/Map 下标与属性访问 */
    private static void listAndMapAccess() {
        printSection("8. 列表/Map 访问");

        StandardEvaluationContext ctx = new StandardEvaluationContext();
        ctx.setVariable("list", List.of("a", "b", "c"));
        ctx.setVariable("map", Map.of("x", 1, "y", 2));

        Expression listIdx = PARSER.parseExpression("#list[1]");
        System.out.println("  #list[1] => " + listIdx.getValue(ctx, String.class));

        Expression mapKey = PARSER.parseExpression("#map['x']");
        System.out.println("  #map['x'] => " + mapKey.getValue(ctx, Integer.class));

        Expression mapKeyY = PARSER.parseExpression("#map['y']");
        System.out.println("  #map['y'] => " + mapKeyY.getValue(ctx, Integer.class));
    }

    private static void printSection(String title) {
        System.out.println("\n--- " + title + " ---");
    }

    /** 简单 POJO 用于 #root 演示 */
    public static class Person {
        private final String name;
        private final int age;

        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() { return name; }
        public int getAge() { return age; }
    }
}
