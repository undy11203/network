package org.project.utils;

public class Counter extends Thread  {
    public static final Counter instance = new Counter();

    @Override
    public void run() {
        while (true) {
            // Получаем текущее количество активных потоков
            int activeThreadCount = Thread.activeCount();
            // Выводим количество активных потоков
            System.out.println("");
            System.out.println("Количество активных потоков: " + activeThreadCount);
            Thread.getAllStackTraces().keySet().forEach(thread -> {

                System.out.println("Название потока: " + thread.getName());

            });

            try {
                // Задержка 1 секунда
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // Обрабатываем прерывание потока
                System.out.println("Поток прерван");
                break; // Выходим из цикла при прерывании
            }
        }
    }
}
