import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/*

Требования:
1. Калькулятор умеет выполнять операции сложения, вычитания, умножения и
деления с ТРЕМЯ числами: а + b - c,
                         а - b + c,
                         а * b - c,
                         а / b * с.
2. На вход могут подаваться и два числа.
3. Калькулятор должен принимать на вход числа от 1 до 10 включительно, не
более. На выходе числа не ограничиваются по величине и могут быть
любыми.
4. Калькулятор умеет работать только с целыми числами.
5. При вводе пользователем неподходящих чисел приложение выбрасывает
исключение и завершает свою работу.
6. При вводе пользователем строки, не соответствующей одной из
вышеописанных арифметических операций, приложение выбрасывает
исключение и завершает свою работу.

 */



public class Main {

    public static void main(String[] args) {
        // Создаём объект reader для чтения ввода с консоли
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        print("Калькулятор запущен.\n" +
                "Что нужно решить: ");

        String userInput = "";
        // Пытаемся прочитать строку, введённую в консоль и удаляем лишние пробелы
        try {
            userInput = (reader.readLine()).trim();
        }
        // Отлов ошибок
        catch (IOException e) {
            e.printStackTrace();
        }
        // Закрытие потока для чтения
        finally {
            try {
                reader.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        // Проверка через regex на наличие лишних или недопустимых символов.
        // -? -- есть ли - в начале
        // d+ -- цифры
        // s* -- пробелы
        // ([+\-*/]\s*\d+)* -- операторы + - * / с пробелами (s) ноль или больше, после оператора
        //                     цифры (d) одна или больше, * -- повторение группы ноль и более раз.
        Pattern pattern = Pattern.compile("-?\\d+\\s*([+\\-*/]\\s*\\d+)*");
        Matcher matcher = pattern.matcher(userInput);

        if (matcher.matches()) {
            Handler(userInput);
        }
        else {
            print("Уравнение содержит недопустимые символы, не целые числа или составлено не верно.\n");
        }
    }

    /**
     * Обработчик уравнения. Включает в себя проверки на первое отрицательное число и наличие числа больше 10.
     *
     * @param equation уравнение
     */
    public static void Handler(String equation) {
        equation = equation.replace(" ", "").trim();
        // Для избавления от первого пустого элемента заменяются цифры на пробел, .trim() для удаления первого
        String[] operationParts = equation.replaceAll("\\d+", " ").trim().split(" ");
        String[] numbersParts = equation.replaceAll("\\D+", " ").trim().split(" ");

        // Преобразование массива из String в int
        int[] numbers = Arrays.stream(numbersParts)
                .filter(string -> !string.isEmpty())        // Фильтр для пустых строк на случай "-" в начале
                .mapToInt(Integer::parseInt)        // Само преобразование в int
                .toArray();     // Преобразование в массив

        // Проверка на содержание числа больше 10 в массиве
        if (Arrays.stream(numbers).anyMatch(num -> num > 10)) {
            printn("В соответствии с 3 требованием, а именно: \n\"3. Калькулятор должен принимать на вход числа от 1 до 10 " +
                    "включительно, не более.\" выполнение программы было остановлено.");
        }
        else {
            // Проверка на кол-во знаков. Если их кол-во равно кол-ву цифр/чисел, значит первый элемент отрицательный
            if (numbersParts.length == operationParts.length) {
                numbers[0] = -numbers[0];
                // Избавляемся от "-" в начале.
                for (int i = 0; i < operationParts.length - 1; i++) {
                    operationParts[i] = operationParts[i + 1];
                }
                operationParts = Arrays.copyOf(operationParts, operationParts.length - 1);
                printn("Ответ: " + calculateWithPriority(numbers, operationParts));
            }
            else {
                printn("Ответ: " + calculateWithPriority(numbers, operationParts));
            }
        }

    }

    /**
     * Решение уравнения с приоритетными операциями и без них.
     * Включает в себя проверки на наличие приоритета.
     *
     * @param numbers массив с числами
     * @param operations массив с операциями
     */
    public static int calculateWithPriority(int[] numbers, String[] operations) {
        int sum;
        // Вычисление с приоритетом
        if (hasHigherPriority(operations)) {
            sum = priorityOperation(numbers, operations);
        }
        // Вычисления без приоритета
        else {
            for (int i = 0; i < numbers.length - 1; i++) {
                numbers[i + 1] = performOperation(numbers[i], numbers[i + 1], operations[i]);
            }
            sum = numbers[numbers.length - 1];
        }
        return sum;
    }

    /**
     * Решение уравнения с приоритетными операциями.
     *
     * @param numbers массив с числами
     * @param operations массив с операциями
     */
    public static int priorityOperation(int[] numbers, String[] operations) {
        // Создаём массив для сохранения "-" и "+", которые содержатся в уравнении.
        String[] nonPriorityOperations = {};
        // Создаём массив для сохранения новых чисел, полученных из приоритетных операций (умножение и деление)
        int[] simplifiedEquationNumbers  = {};

        // Проходим по всему уравнению
        for (int i = 0, j = 0, k = 0; i < numbers.length -1; i++) {
            // Если попадается умножение или деление, то производим решение, перезаписываем массив с расширением на 1
            // и сохраняем туда результат
            if (operations[i].equals("*") || operations[i].equals("/")) {
                simplifiedEquationNumbers  = Arrays.copyOf(simplifiedEquationNumbers , simplifiedEquationNumbers .length + 1);
                simplifiedEquationNumbers [j] = performOperation(numbers[i], numbers[i + 1], operations[i]);
                j++;
            }
            // Если попадается сложение или вычитание, то сохраняем + или - в массив с расширением на 1
            if (operations[i].equals("+") || operations[i].equals("-")) {
                // Если следующая операция является не приоритетной (минус или плюс), то так же сохраняем число в массив
                if (i + 1 < operations.length && (operations[i + 1].equals("+") || operations[i + 1].equals("-"))){
                    simplifiedEquationNumbers  = Arrays.copyOf(simplifiedEquationNumbers , simplifiedEquationNumbers .length + 1);
                    simplifiedEquationNumbers [j] = numbers[i + 1];
                    j++;
                }
                // Для сохранения последнего числа в случае несоответствия предыдущим условиям
                else if (i + 1 >= operations.length) {
                    simplifiedEquationNumbers  = Arrays.copyOf(simplifiedEquationNumbers , simplifiedEquationNumbers .length + 1);
                    simplifiedEquationNumbers [j] = numbers[i + 1];
                    j++;
                }
                // Для сохранения первого числа в случае несоответствия предыдущим условиям
                else {
                    simplifiedEquationNumbers  = Arrays.copyOf(simplifiedEquationNumbers , simplifiedEquationNumbers .length + 1);
                    simplifiedEquationNumbers [j] = numbers[i];
                    j++;
                }
                nonPriorityOperations = Arrays.copyOf(nonPriorityOperations, nonPriorityOperations.length + 1);
                nonPriorityOperations[k] = operations[i];
                k++;
            }
        }

        // После вычислений, производим решение уравнения таким же способом, как если бы решали без * и /
        for (int i = 0; i < simplifiedEquationNumbers .length - 1; i++) {
            simplifiedEquationNumbers [i + 1] = performOperation(simplifiedEquationNumbers [i], simplifiedEquationNumbers [i + 1], nonPriorityOperations[i]);
        }
        // Возвращаем последнее значение в массиве, так как все операции записываются в массиве, для того, чтобы
        // не создавать лишних переменных и/или массивов, так как далее этот массив нигде не используется.
        return simplifiedEquationNumbers [simplifiedEquationNumbers .length - 1];
    }

    /**
     * Решение уравнения из двух чисел.
     *
     * @param num1 первое число
     * @param num2 второе число
     * @param operation операция
     */
    public static int performOperation(int num1, int num2, String operation) {
        char operationChar = operation.charAt(0);
        return switch (operationChar) {
            case '+' -> num1 + num2;
            case '-' -> num1 - num2;
            case '*' -> num1 * num2;
            case '/' -> num1 / num2;
            //  В случае несовпадения
            default -> throw new IllegalArgumentException("Неверная операция " + operation);
        };
    }

    /**
     * Проверка на наличие приоритетных операторов.
     *
     * @param operations массив операций для проверки
     */
    public static boolean hasHigherPriority(String[] operations) {
        return Arrays.asList(operations).contains("*") || Arrays.asList(operations).contains("/");
    }


    /**
     * Выводит значение в консоль с использованием print. Работает с любыми типами данных.
     *
     * @param message сообщение для вывода
     */
    public static void print(Object message) {
        if (message instanceof Object[]) {
            System.out.print(Arrays.deepToString((Object[]) message));
        } else if (message instanceof int[]) {
            System.out.print(Arrays.toString((int[]) message));
        } else {
            System.out.print(message);
        }
    }

    /**
     * Выводит значение в консоль с использованием println. Работает с любыми типами данных.
     *
     * @param message сообщение для вывода
     */
    public static void printn(Object message) {
        if (message instanceof Object[]) {
            System.out.println(Arrays.deepToString((Object[]) message));
        } else if (message instanceof int[]) {
            System.out.println(Arrays.toString((int[]) message));
        } else {
            System.out.println(message);
        }
    }

}