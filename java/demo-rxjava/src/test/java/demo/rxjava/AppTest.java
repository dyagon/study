package demo.rxjava;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AppTest {

    @Test
    public void testDishCreation() {
        System.out.println(">>> 测试开始：创建 Dish 对象");
        App.Dish dish = new App.Dish(1);
        System.out.println(">>> Dish 对象已创建: " + dish);
        assertNotNull(dish);
        assertEquals("Dish 1", dish.toString());
        System.out.println(">>> 测试通过：Dish 创建和 toString() 验证成功");
    }

    @Test
    public void testDishToString() {
        System.out.println(">>> 测试开始：测试 Dish 的 toString() 方法");
        App.Dish dish1 = new App.Dish(42);
        App.Dish dish2 = new App.Dish(100);
        
        System.out.println(">>> dish1.toString() = " + dish1.toString());
        System.out.println(">>> dish2.toString() = " + dish2.toString());
        
        assertEquals("Dish 42", dish1.toString());
        assertEquals("Dish 100", dish2.toString());
        System.out.println(">>> 测试通过：toString() 方法验证成功");
    }

    @Test
    public void testSleepMillis() {
        System.out.println(">>> 测试开始：测试 sleepMillis() 方法");
        long startTime = System.currentTimeMillis();
        App.sleepMillis(100);
        long endTime = System.currentTimeMillis();
        
        long elapsed = endTime - startTime;
        System.out.println(">>> 实际睡眠时间: " + elapsed + " 毫秒");
        assertTrue(elapsed >= 100, "睡眠时间应该至少为 100 毫秒");
        System.out.println(">>> 测试通过：sleepMillis() 方法验证成功");
    }
}
