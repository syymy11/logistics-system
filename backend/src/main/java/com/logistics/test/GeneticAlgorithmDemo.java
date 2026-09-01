package com.logistics.test;

import com.logistics.algorithm.GeneticAlgorithm;
import java.util.ArrayList;
import java.util.List;

public class GeneticAlgorithmDemo {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("      遗传算法VRP路径规划演示");
        System.out.println("========================================");

        String[] pointNames = {
            "仓库",
            "客户A-昌邑区解放大路156号",
            "客户B-龙潭区福州街88号",
            "客户C-船营区松江西路205号",
            "客户D-丰满区吉林大街199号",
            "客户E-昌邑区吉林大街128号"
        };

        double[] lng = {126.5500, 126.5520, 126.6250, 126.4580, 126.5650, 126.5480};
        double[] lat = {43.8400, 43.8450, 43.9180, 43.8280, 43.8080, 43.8380};
        double[] weights = {0, 120.5, 85.0, 230.0, 65.8, 150.0};

        int n = lng.length;
        double[][] distanceMatrix = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    distanceMatrix[i][j] = 0;
                } else {
                    double dx = (lng[i] - lng[j]) * 85.0;
                    double dy = (lat[i] - lat[j]) * 111.0;
                    distanceMatrix[i][j] = Math.sqrt(dx * dx + dy * dy);
                }
            }
        }

        System.out.println("\n【测试数据】");
        System.out.println("车辆载重上限: 500kg");
        System.out.println("\n各点位置和需求量:");
        for (int i = 0; i < n; i++) {
            System.out.printf("  %d: %s, 需求: %.1fkg\n", i, pointNames[i], weights[i]);
        }

        System.out.println("\n【距离矩阵 (公里)】");
        System.out.print("       ");
        for (int j = 0; j < n; j++) {
            System.out.printf("%6d ", j);
        }
        System.out.println();
        for (int i = 0; i < n; i++) {
            System.out.printf("%d: ", i);
            for (int j = 0; j < n; j++) {
                System.out.printf("%6.2f ", distanceMatrix[i][j]);
            }
            System.out.println();
        }

        List<Double> demands = new ArrayList<>();
        for (double w : weights) {
            demands.add(w);
        }

        double vehicleCapacity = 500.0;

        System.out.println("\n========================================");
        System.out.println("【方案一：原始顺序（未优化）】");
        System.out.println("========================================");

        List<Integer> naiveOrder = new ArrayList<>();
        for (int i = 1; i < n; i++) {
            naiveOrder.add(i);
        }

        GeneticAlgorithm ga1 = new GeneticAlgorithm(distanceMatrix, demands, vehicleCapacity);
        GeneticAlgorithm.Solution naiveSolution = decodeManual(ga1, naiveOrder, distanceMatrix, demands, vehicleCapacity, pointNames);

        System.out.println("\n路径顺序: 仓库 → ");
        for (int i = 1; i < n; i++) {
            System.out.printf("  %s → \n", pointNames[i]);
        }
        System.out.println("  仓库");
        System.out.printf("\n总距离: %.2f 公里\n", naiveSolution.totalDistance);
        System.out.printf("总载重: %.1f kg\n", getTotalWeight(weights));

        System.out.println("\n========================================");
        System.out.println("【方案二：遗传算法优化】");
        System.out.println("========================================");

        GeneticAlgorithm ga2 = new GeneticAlgorithm(distanceMatrix, demands, vehicleCapacity);
        GeneticAlgorithm.Solution optimizedSolution = ga2.run();

        System.out.println("\n优化后的路径:");
        int routeNum = 1;
        for (List<Integer> route : optimizedSolution.routes) {
            System.out.printf("\n路线 %d: ", routeNum++);
            double routeWeight = 0;
            for (int i = 0; i < route.size(); i++) {
                int node = route.get(i);
                System.out.print(pointNames[node]);
                routeWeight += weights[node];
                if (i < route.size() - 1) {
                    System.out.print(" → ");
                }
            }
            System.out.printf("\n  该路线载重: %.1f kg (容量: %.0f kg)\n", routeWeight, vehicleCapacity);
        }

        System.out.println("\n========================================");
        System.out.println("【效果对比】");
        System.out.println("========================================");
        System.out.printf("原始顺序总距离:  %.2f 公里\n", naiveSolution.totalDistance);
        System.out.printf("优化后总距离:    %.2f 公里\n", optimizedSolution.totalDistance);
        double saving = naiveSolution.totalDistance - optimizedSolution.totalDistance;
        double savingPercent = (saving / naiveSolution.totalDistance) * 100;
        System.out.printf("\n节省距离:        %.2f 公里 (%.1f%%)\n", saving, savingPercent);

        System.out.println("\n【遗传算法的作用说明】");
        System.out.println("1. 路径优化：通过交叉、变异操作探索不同的配送顺序");
        System.out.println("2. 载重约束：解码时自动检查车辆载重，超载则分多趟");
        System.out.println("3. 目标函数：最小化总行驶距离");
        System.out.println("4. 选择策略：保留更优路径，淘汰较差路径");
    }

    private static GeneticAlgorithm.Solution decodeManual(
            GeneticAlgorithm ga,
            List<Integer> chromosome,
            double[][] distanceMatrix,
            List<Double> demands,
            double vehicleCapacity,
            String[] pointNames) {

        GeneticAlgorithm.Solution solution = new GeneticAlgorithm.Solution();
        List<Integer> currentRoute = new ArrayList<>();
        currentRoute.add(0);
        double currentLoad = 0;

        for (int customer : chromosome) {
            double demand = demands.get(customer);
            if (currentLoad + demand > vehicleCapacity && currentRoute.size() > 1) {
                currentRoute.add(0);
                solution.routes.add(new ArrayList<>(currentRoute));
                currentRoute.clear();
                currentRoute.add(0);
                currentLoad = 0;
            }
            currentRoute.add(customer);
            currentLoad += demand;
        }
        currentRoute.add(0);
        solution.routes.add(currentRoute);

        double totalDist = 0;
        for (List<Integer> route : solution.routes) {
            for (int i = 0; i < route.size() - 1; i++) {
                totalDist += distanceMatrix[route.get(i)][route.get(i + 1)];
            }
        }
        solution.totalDistance = totalDist;
        solution.fitness = 1.0 / (totalDist + 1);

        return solution;
    }

    private static double getTotalWeight(double[] weights) {
        double sum = 0;
        for (int i = 1; i < weights.length; i++) {
            sum += weights[i];
        }
        return sum;
    }
}
