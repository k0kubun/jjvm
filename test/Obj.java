public class Obj {
    public static void main(String[] args) {
        Person person = new Person(26);
        person.getAge();
    }

    private static class Person {
        private final int age;

        public Person(int age) {
            this.age = age;
        }

        public int getAge() {
            return age;
        }
    }
}
