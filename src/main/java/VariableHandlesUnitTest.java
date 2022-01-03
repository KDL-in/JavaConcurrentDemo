import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

public class VariableHandlesUnitTest {
    public int publicTestVariable = 1;
    private int privateTestVariable = 1;
    public int variableToSet = 1;
    public int variableToCompareAndSet = 1;
    public int variableToGetAndAdd = 0;
    public byte variableToBitwiseOr = 0;


    static VarHandle PUBLIC_TEST_VARIABLE;
    static VarHandle arrayVarHandle = MethodHandles.arrayElementVarHandle(int[].class);
    static {
        try {
            PUBLIC_TEST_VARIABLE = MethodHandles
                    .lookup()
                    .in(VariableHandlesUnitTest.class)
                    .findVarHandle(VariableHandlesUnitTest.class, "publicTestVariable", int.class);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.out.println(arrayVarHandle.coordinateTypes());

    }
}