// service/AmapService.java
package com.logistics.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class AmapService {

    @Value("${amap.key}")
    private String apiKey;

    private final OkHttpClient httpClient = new OkHttpClient();

    /**
     * 地址转坐标（地理编码）
     */
    public String[] geocode(String address) throws Exception {
        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException("地址不能为空");
        }
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new Exception("高德地图API密钥未配置");
        }

        String url = String.format(
                "https://restapi.amap.com/v3/geocode/geo?address=%s&key=%s",
                address, apiKey);

        Request request = new Request.Builder().url(url).build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new Exception("高德地图API请求失败，状态码：" + response.code());
            }

            String body = response.body().string();
            if (body == null || body.isEmpty()) {
                throw new Exception("高德地图API返回空数据");
            }

            JSONObject json = JSONObject.parseObject(body);
            if (json == null) {
                throw new Exception("高德地图API返回数据解析失败");
            }

            if ("1".equals(json.getString("status"))) {
                JSONArray geocodes = json.getJSONArray("geocodes");
                if (geocodes != null && geocodes.size() > 0) {
                    String location = geocodes.getJSONObject(0).getString("location");
                    if (location != null && !location.isEmpty()) {
                        return location.split(",");
                    }
                }
            } else {
                String errorInfo = json.getString("info");
                throw new Exception("高德地图API返回错误：" + errorInfo);
            }
        } catch (IOException e) {
            throw new Exception("网络请求失败：" + e.getMessage());
        }
        return new String[] { "0", "0" };
    }

    /**
     * 计算两点之间的驾车距离（米）
     */
    public int getDistance(String originLng, String originLat, String destLng, String destLat) throws Exception {
        if (originLng == null || originLat == null || destLng == null || destLat == null) {
            throw new IllegalArgumentException("坐标参数不能为空");
        }

        // 尝试使用高德地图API
        try {
            if (apiKey != null && !apiKey.trim().isEmpty()) {
                String url = String.format(
                        "https://restapi.amap.com/v3/distance?origins=%s,%s&destination=%s,%s&type=1&key=%s",
                        originLng, originLat, destLng, destLat, apiKey);

                Request request = new Request.Builder().url(url).build();
                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        String body = response.body().string();
                        if (body != null && !body.isEmpty()) {
                            JSONObject json = JSONObject.parseObject(body);
                            if (json != null && "1".equals(json.getString("status"))) {
                                JSONArray results = json.getJSONArray("results");
                                if (results != null && results.size() > 0) {
                                    return results.getJSONObject(0).getInteger("distance");
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // 高德地图API调用失败，使用默认距离计算
            System.out.println("高德地图API调用失败，使用默认距离计算：" + e.getMessage());
        }

        // 使用Haversine公式计算两点之间的直线距离（米）
        double lat1 = Double.parseDouble(originLat);
        double lon1 = Double.parseDouble(originLng);
        double lat2 = Double.parseDouble(destLat);
        double lon2 = Double.parseDouble(destLng);

        final int R = 6371000; // 地球半径（米）
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        int distance = (int) (R * c);

        // 直线距离乘以1.5作为驾车距离的估算
        return distance * 3 / 2;
    }

    /**
     * 批量计算距离矩阵（使用本地Haversine公式，不调用API，速度更快）
     */
    public double[][] getDistanceMatrix(List<String[]> locations) throws Exception {
        if (locations == null || locations.isEmpty()) {
            throw new IllegalArgumentException("位置列表不能为空");
        }

        int n = locations.size();
        double[][] matrix = new double[n][n];

        for (int i = 0; i < n; i++) {
            String[] location1 = locations.get(i);
            if (location1 == null || location1.length < 2) {
                throw new IllegalArgumentException("位置坐标格式错误");
            }

            for (int j = i + 1; j < n; j++) {
                String[] location2 = locations.get(j);
                if (location2 == null || location2.length < 2) {
                    throw new IllegalArgumentException("位置坐标格式错误");
                }

                int dist = getLocalDistance(location1[0], location1[1], location2[0], location2[1]);
                matrix[i][j] = dist / 1000.0;
                matrix[j][i] = matrix[i][j];
            }
            matrix[i][i] = 0;
        }
        return matrix;
    }

    /**
     * 使用Haversine公式本地计算两点之间的直线距离（米），不调用API，速度更快
     */
    private int getLocalDistance(String originLng, String originLat, String destLng, String destLat) {
        double lat1 = Double.parseDouble(originLat);
        double lon1 = Double.parseDouble(originLng);
        double lat2 = Double.parseDouble(destLat);
        double lon2 = Double.parseDouble(destLng);

        final int R = 6371000;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        int distance = (int) (R * c);
        return distance * 3 / 2;
    }

    /**
     * 获取路径规划的坐标串（用于前端绘制路线）
     */
    public String getRoutePath(String origin, String destination) throws Exception {
        if (origin == null || origin.trim().isEmpty() || destination == null || destination.trim().isEmpty()) {
            throw new IllegalArgumentException("起点和终点不能为空");
        }
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new Exception("高德地图API密钥未配置");
        }

        String url = String.format(
                "https://restapi.amap.com/v3/direction/driving?origin=%s&destination=%s&key=%s",
                origin, destination, apiKey);

        Request request = new Request.Builder().url(url).build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new Exception("高德地图API请求失败，状态码：" + response.code());
            }

            String body = response.body().string();
            if (body == null || body.isEmpty()) {
                throw new Exception("高德地图API返回空数据");
            }

            JSONObject json = JSONObject.parseObject(body);
            if (json == null) {
                throw new Exception("高德地图API返回数据解析失败");
            }

            if ("1".equals(json.getString("status"))) {
                JSONObject route = json.getJSONObject("route");
                if (route != null) {
                    JSONArray paths = route.getJSONArray("paths");
                    if (paths != null && paths.size() > 0) {
                        return paths.getJSONObject(0).getString("polyline");
                    }
                }
            } else {
                String errorInfo = json.getString("info");
                throw new Exception("高德地图API返回错误：" + errorInfo);
            }
        } catch (IOException e) {
            throw new Exception("网络请求失败：" + e.getMessage());
        }
        return "";
    }
}