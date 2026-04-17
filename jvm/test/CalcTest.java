

public class CalcTest {

    public int calc() {
        int a = 100;
        int b = 200;
        int c = 300;
        return (a + b) * c;
    }

    public static void main(String[] args) {
        CalcTest calcTest = new CalcTest();
        int result = calcTest.calc();
        System.out.println(result);
    }
    
}
