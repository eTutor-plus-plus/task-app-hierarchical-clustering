package at.jku.dke.task_app.hierarchical_clustering.generators.dendrogram;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DendrogramSvgRenderer {

    // ---- layout constants (pixels) - not local variables for potential dynamic tuning feature ----
    private final int canvasWidth = 900;
    private final int canvasHeight = 600;
    private final int marginTop = 40;
    private final int marginBottom = 60;
    private final int marginLeft = 70;
    private final int marginRight = 30;

    private final int leafLabelOffset = 5;
    private final int yAxisTickLength = 5;
    private final int yAxisLabelGap = 4;

    private final int leafLabelFontSize = 20;
    private final int axisFontSize = 16;
    private final int lineWidth = 2;

    private final String lineColor = "#000000";
    private final String gridColor = "#B7BFC7";
    private final String textColor = "#222222";
    private final String axisColor = "#303030";
    private final String backgroundColor = "#ffffff";

    public String render(DendrogramModel model) {
        List<String> leaves = model.getLeafOrder();
        DendrogramModel.Node root = model.getRoot();
        int n = leaves.size();

        int plotW = canvasWidth - marginLeft - marginRight;
        int plotH = canvasHeight - marginTop - marginBottom;

        double colW = (double) plotW / n;

        Map<String, Double> leafX = new HashMap<>();
        for (int i = 0; i < n; i++) {
            leafX.put(leaves.get(i), marginLeft + colW * i + colW / 2.0);
        }

        double maxHeight = root.getHeight();
        double tickStep = tickStep(maxHeight);
        double fitMax = Math.ceil(maxHeight / tickStep) * tickStep;

        StringBuilder sb = new StringBuilder();

        // --- SVG header ---
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append(format(
            "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"%d\" height=\"%d\" viewBox=\"0 0 %d %d\">\n",
            canvasWidth, canvasHeight, canvasWidth, canvasHeight));

        // background
        sb.append(format(
            "  <rect width=\"%d\" height=\"%d\" fill=\"%s\"/>\n",
            canvasWidth, canvasHeight, backgroundColor));

        // --- grid ---
        sb.append("  <g id=\"grid\">\n");

        double gridStep = tickStep / 2.0;

        double gridtick = 0.0;
        while (gridtick <= fitMax + gridStep * 0.001) {
            double svgY = heightToSvgY(gridtick, fitMax, plotH);

            sb.append(format(
                "    <line x1=\"%d\" y1=\"%.2f\" x2=\"%d\" y2=\"%.2f\" stroke=\"%s\" stroke-width=\"1\" stroke-dasharray=\"4,3\"/>\n",
                marginLeft, svgY, marginLeft + plotW, svgY, gridColor));

            sb.append(format(
                "    <line x1=\"%d\" y1=\"%.2f\" x2=\"%d\" y2=\"%.2f\" stroke=\"%s\" stroke-width=\"1\"/>\n",
                marginLeft - yAxisTickLength, svgY, marginLeft, svgY, axisColor));

            String tickLabel = format("%.1f", gridtick);

            sb.append(format(
                "    <text x=\"%d\" y=\"%.2f\" text-anchor=\"end\" dominant-baseline=\"middle\" font-size=\"%d\" fill=\"%s\">%s</text>\n",
                marginLeft - yAxisTickLength - yAxisLabelGap, svgY, axisFontSize, axisColor, tickLabel));

            gridtick += gridStep;
        }

        sb.append("  </g>\n");

        // y-axis
        sb.append(format(
            "  <line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\" stroke=\"%s\" stroke-width=\"1.5\"/>\n",
            marginLeft, marginTop, marginLeft, marginTop + plotH, axisColor));

        // vertical "Distance / Height" text (to be deleted)
//        sb.append(format(
//            "  <text transform=\"rotate(-90)\" x=\"%d\" y=\"%d\" text-anchor=\"middle\" font-size=\"%d\" fill=\"%s\">Distance / Height</text>\n",
//            -(marginTop + plotH / 2), marginLeft - 45, axisFontSize + 1, axisColor));

        // --- dendrogram ---
        sb.append(format(
            "  <g id=\"dendrogram\" stroke=\"%s\" stroke-width=\"%d\" fill=\"none\">\n",
            lineColor, lineWidth));

        Map<String, Double> nodeX = new HashMap<>(leafX);
        renderNode(root, nodeX, fitMax, plotH, sb);

        sb.append("  </g>\n");

        // --- leaf labels ---
        sb.append("  <g id=\"labels\">\n");

        double leafY = marginTop + plotH + leafLabelOffset + leafLabelFontSize * 0.8;

        for (String leaf : leaves) {
            double x = leafX.get(leaf);

            sb.append(format(
                "    <text x=\"%.2f\" y=\"%.2f\" text-anchor=\"middle\" font-size=\"%d\" fill=\"%s\">%s</text>\n",
                x, leafY, leafLabelFontSize, textColor, escapeXml(leaf)));
        }

        sb.append("  </g>\n");

        // x-axis
        sb.append(format(
            "  <line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\" stroke=\"%s\" stroke-width=\"1.5\"/>\n",
            marginLeft, marginTop + plotH, marginLeft + plotW, marginTop + plotH, axisColor));

        sb.append("</svg>");

        return sb.toString();
    }

    private void renderNode(DendrogramModel.Node node, Map<String, Double> nodeX, double niceMax, int plotH, StringBuilder sb) {
        if (node.isLeaf()) return;

        renderNode(node.getLeft(), nodeX, niceMax, plotH, sb);
        renderNode(node.getRight(), nodeX, niceMax, plotH, sb);

        double xL = nodeX.get(node.getLeft().getLabel());
        double xR = nodeX.get(node.getRight().getLabel());

        double svgYThis = heightToSvgY(node.getHeight(), niceMax, plotH);
        double svgYL = heightToSvgY(node.getLeft().getHeight(), niceMax, plotH);
        double svgYR = heightToSvgY(node.getRight().getHeight(), niceMax, plotH);

        sb.append(format(
            "    <line x1=\"%.2f\" y1=\"%.2f\" x2=\"%.2f\" y2=\"%.2f\"/>\n",
            xL, svgYThis, xR, svgYThis));

        sb.append(format(
            "    <line x1=\"%.2f\" y1=\"%.2f\" x2=\"%.2f\" y2=\"%.2f\"/>\n",
            xL, svgYThis, xL, svgYL));

        sb.append(format(
            "    <line x1=\"%.2f\" y1=\"%.2f\" x2=\"%.2f\" y2=\"%.2f\"/>\n",
            xR, svgYThis, xR, svgYR));

        nodeX.put(node.getLabel(), (xL + xR) / 2.0);
    }

    private double heightToSvgY(double height, double niceMax, int plotH) {
        return marginTop + plotH * (1.0 - height / niceMax);
    }

    static double tickStep(double maxHeight) {
        double roughStep = maxHeight / 7.0;
        double magnitude = Math.pow(10, Math.floor(Math.log10(roughStep)));
        double[] multipliers = {1.0, 2.0, 5.0, 10.0};

        for (double m : multipliers) {
            double candidate = magnitude * m;
            if (maxHeight / candidate <= 10.0) return candidate;
        }

        return magnitude * 10;
    }

    private static String escapeXml(String s) {
        return s.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;");
    }


    private static String format(String format, Object... args) {
        return String.format(Locale.ENGLISH, format, args);
    }
}
