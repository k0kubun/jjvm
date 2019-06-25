public class Interface {
    public static void main(String[] args) {
        Animal animal = new Dog();
        animal.cry();
    }

    interface Animal {
        void cry();
    }

    private static class Dog implements Animal {
        public void cry() {
            System.out.println("Waon");
        }
    }
}
