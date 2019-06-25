public class Obj {
    public static void main(String[] args) {
        Person person = new Person(26);
        System.out.println(person.getAge());
        System.out.println(person.isHealthy() ? "healthy" : "unhealthy");

        SeniorPerson senior = new SeniorPerson(60);
        System.out.println(senior.getAge());
        System.out.println(senior.isHealthy() ? "healthy" : "unhealthy");
    }

    private static class Person {
        private final int age;

        public Person(int age) {
            this.age = age;
        }

        public int getAge() {
            return age;
        }

        public boolean isHealthy() {
            return true;
        }
    }

    private static class SeniorPerson extends Person {
        public SeniorPerson(int age) {
            super(age);
        }

        public boolean isHealthy() {
            return false;
        }
    }
}
