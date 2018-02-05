package com.spider;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Eric
 * @date 2018/2/2 10:31
 * Description
 */
public class ImageSpider {

    // private static final String domain = "https://www.doutula.com";
    private static final String domain = "https://www.52doutu.cn/search/666";
    private static final String REGEX_52DOUTU = "href=\"((https?:\\/\\/)\\S+(\\.(jpe?g|png|gif))|(http:\\/\\/\\S+\\.pic\\.sogou\\.com\\/\\S{16}))";

    public static void main(String[] args) throws IOException {
        HttpUriRequest uriRequest = new HttpGet(domain);
        uriRequest.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.84 Safari/537.36");
        CloseableHttpResponse response = HttpClientBuilder.create().build().execute(uriRequest);

        InputStream inputStream = response.getEntity().getContent();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder lineBuilder = new StringBuilder();
        bufferedReader.lines().forEach(lineBuilder::append);
        String pageContent = lineBuilder.toString();

        List<String> imgUrls = getImgUrlsInContent(pageContent, REGEX_52DOUTU);
        System.out.println(imgUrls);

        imgUrls.forEach((url) -> {
            InputStream imageStream;
            final String fileName = getImgFileName(url);
            System.out.println("save image: " + fileName);
            final String suffix = getSuffix(fileName);
            try {
                File tofile = new File("imgs/" + fileName);
                boolean fileExist = tofile.exists() || tofile.createNewFile();
                imageStream = new URL(url).openConnection().getInputStream();

                BufferedImage bufferedImage = ImageIO.read(imageStream);
                if (fileExist) {
                    ImageIO.write(bufferedImage, suffix, tofile);
                }
            } catch (Exception e1) {
                System.out.println("Error Image Url: " + url);
                e1.printStackTrace();
            }
        });

        // Connection connection = Jsoup.connect(domain);
        // connection.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.84 Safari/537.36");
        // Document document = connection.get();
        // System.out.println(document.outerHtml());
    }

    private static List<String> getImgUrlsInContent(final String content, final String regex) {
        List<String> imgUrls = new ArrayList<>();
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            imgUrls.add(matcher.group(1));
        }
        return imgUrls;
    }

    private static String getImgFileName(final String imgUrl) {
        final String suffixRegex = "[\\w\\d]+\\.(jpe?g|png|gif)";
        final String nonSuffixRegex = "[\\w\\d]+$";
        Pattern suffixPattern;
        Pattern nonSuffixPattern;
        Matcher suffixMatcher;
        Matcher nonSuffixMatcher;

        suffixPattern = Pattern.compile(suffixRegex, Pattern.CASE_INSENSITIVE);
        nonSuffixPattern = Pattern.compile(nonSuffixRegex, Pattern.CASE_INSENSITIVE);
        suffixMatcher = suffixPattern.matcher(imgUrl);
        nonSuffixMatcher = nonSuffixPattern.matcher(imgUrl);
        String fileName = "";
        if (suffixMatcher.find()) {
            fileName = suffixMatcher.group();
        } else if (nonSuffixMatcher.find()) {
            fileName = nonSuffixMatcher.group() + ".gif";
        }
        return fileName;
    }

    private static String getSuffix(final String fileName) {
        final String regex = "(jpe?g|png|gif)";
        Pattern pattern;
        Matcher matcher;

        pattern = Pattern.compile(regex);
        matcher = pattern.matcher(fileName);
        if (matcher.find()) {
            return matcher.group();
        }
        return "jpg";
    }
}
