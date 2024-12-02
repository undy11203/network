package org.project;

public class Main {
    public static void main(String[] args) {
        while (true) {
            // Выводим количество активных потоков
            int activeCount = Thread.activeCount();
            System.out.println("Количество активных потоков: " + activeCount);
            try {
                Thread.sleep(1000); // Задержка на 1 секунду
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break; // Выходим из цикла если поток прерван
            }
        }
    }
}