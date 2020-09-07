package maze;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("Please, enter the size of a maze");
        final Scanner scanner = new Scanner(System.in);
        final int height = scanner.nextInt();
        final int width = scanner.nextInt();
        System.out.println(Maze.random(height, width));
    }
}
