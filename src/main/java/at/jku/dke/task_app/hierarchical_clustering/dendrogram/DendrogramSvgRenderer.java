package at.jku.dke.task_app.hierarchical_clustering.dendrogram;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DendrogramSvgRenderer {

    public String render(DendrogramModel model) {
        List<String> leaves = model.getLeafOrder();
        DendrogramModel.Node root = model.getRoot();
        int n = leaves.size();

        int plotW = Config.canvasWidth - Config.marginLeft - Config.marginRight;
        int plotH = Config.canvasHeight - Config.marginTop - Config.marginBottom;

        double colW = (double) plotW / n;

        Map<String, Double> leafX = new HashMap<>();
        for (int i = 0; i < n; i++) {
            leafX.put(leaves.get(i), Config.marginLeft + colW * i + colW / 2.0);
        }

        double maxHeight = root.getHeight();
        double tickStep = tickStep(maxHeight);
        double fitMax = Math.ceil(maxHeight / tickStep) * tickStep;

        StringBuilder sb = new StringBuilder();

        // --- SVG header ---
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append(format(
            "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"%d\" height=\"%d\" viewBox=\"0 0 %d %d\">\n",
            Config.canvasWidth, Config.canvasHeight, Config.canvasWidth, Config.canvasHeight));

        // background
        sb.append(format(
            "  <rect width=\"%d\" height=\"%d\" fill=\"%s\"/>\n",
            Config.canvasWidth, Config.canvasHeight, Config.backgroundColor));

        // --- grid ---
        sb.append("  <g id=\"grid\">\n");

        double gridStep = tickStep / 2.0;

        double gridtick = 0.0;
        while (gridtick <= fitMax + gridStep * 0.001) {
            double svgY = heightToSvgY(gridtick, fitMax, plotH);

            sb.append(format(
                "    <line x1=\"%d\" y1=\"%.2f\" x2=\"%d\" y2=\"%.2f\" stroke=\"%s\" stroke-width=\"1\" stroke-dasharray=\"4,3\"/>\n",
                Config.marginLeft, svgY, Config.marginLeft + plotW, svgY, Config.gridColor));

            sb.append(format(
                "    <line x1=\"%d\" y1=\"%.2f\" x2=\"%d\" y2=\"%.2f\" stroke=\"%s\" stroke-width=\"1\"/>\n",
                Config.marginLeft - Config.yAxisTickLength, svgY, Config.marginLeft, svgY, Config.axisColor));

            String tickLabel = format("%.1f", gridtick);

            sb.append(format(
                "    <text x=\"%d\" y=\"%.2f\" text-anchor=\"end\" dominant-baseline=\"middle\" font-size=\"%d\" fill=\"%s\">%s</text>\n",
                Config.marginLeft - Config.yAxisTickLength - Config.yAxisLabelGap, svgY, Config.axisFontSize, Config.axisColor, tickLabel));

            gridtick += gridStep;
        }

        sb.append("  </g>\n");

        // y-axis
        sb.append(format(
            "  <line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\" stroke=\"%s\" stroke-width=\"1.5\"/>\n",
            Config.marginLeft, Config.marginTop, Config.marginLeft, Config.marginTop + plotH, Config.axisColor));

        // --- dendrogram ---
        sb.append(format(
            "  <g id=\"dendrogram\" stroke=\"%s\" stroke-width=\"%d\" fill=\"none\">\n",
            Config.lineColor, Config.lineWidth));

        Map<String, Double> nodeX = new HashMap<>(leafX);
        renderNode(root, nodeX, fitMax, plotH, sb);

        sb.append("  </g>\n");

        // --- leaf labels ---
        sb.append("  <g id=\"labels\">\n");

        double leafY = Config.marginTop + plotH + Config.leafLabelOffset + Config.leafLabelFontSize * 0.8;

        for (String leaf : leaves) {
            double x = leafX.get(leaf);

            sb.append(format(
                "    <text x=\"%.2f\" y=\"%.2f\" text-anchor=\"middle\" font-size=\"%d\" fill=\"%s\">%s</text>\n",
                x, leafY, Config.leafLabelFontSize, Config.textColor, escapeXml(leaf)));
        }

        sb.append("  </g>\n");

        // x-axis
        sb.append(format(
            "  <line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\" stroke=\"%s\" stroke-width=\"1.5\"/>\n",
            Config.marginLeft, Config.marginTop + plotH, Config.marginLeft + plotW, Config.marginTop + plotH, Config.axisColor));

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
        return Config.marginTop + plotH * (1.0 - height / niceMax);
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

    @Component
    public static class Config {
        // canvas
        private static int canvasWidth;
        private static int canvasHeight;
        private static int marginTop;
        private static int marginBottom;
        private static int marginLeft;
        private static int marginRight;

        // label gaps to axis
        private static int leafLabelOffset;
        private static int yAxisTickLength;
        private static int yAxisLabelGap;

        // font/line size
        private static int leafLabelFontSize;
        private static int axisFontSize;
        private static int lineWidth;

        // colors
        private static String lineColor;
        private static String gridColor;
        private static String textColor;
        private static String axisColor;
        private static String backgroundColor;

        @Value("${app.dendrogram.canvas.width}")
        public void setCanvasWidth(int canvasWidth) {
            Config.canvasWidth = canvasWidth;
        }

        @Value("${app.dendrogram.canvas.height}")
        public void setCanvasHeight(int canvasHeight) {
            Config.canvasHeight = canvasHeight;
        }

        @Value("${app.dendrogram.canvas.margin.top}")
        public void setMarginTop(int marginTop) {
            Config.marginTop = marginTop;
        }

        @Value("${app.dendrogram.canvas.margin.bottom}")
        public void setMarginBottom(int marginBottom) {
            Config.marginBottom = marginBottom;
        }

        @Value("${app.dendrogram.canvas.margin.left}")
        public void setMarginLeft(int marginLeft) {
            Config.marginLeft = marginLeft;
        }

        @Value("${app.dendrogram.canvas.margin.right}")
        public void setMarginRight(int marginRight) {
            Config.marginRight = marginRight;
        }

        @Value("${app.dendrogram.labels.leaf.offset}")
        public void setLeafLabelOffset(int leafLabelOffset) {
            Config.leafLabelOffset = leafLabelOffset;
        }

        @Value("${app.dendrogram.labels.axis.tick-length}")
        public void setyAxisTickLength(int yAxisTickLength) {
            Config.yAxisTickLength = yAxisTickLength;
        }

        @Value("${app.dendrogram.labels.axis.label-gap}")
        public void setyAxisLabelGap(int yAxisLabelGap) {
            Config.yAxisLabelGap = yAxisLabelGap;
        }

        @Value("${app.dendrogram.labels.leaf.font-size}")
        public void setLeafLabelFontSize(int leafLabelFontSize) {
            Config.leafLabelFontSize = leafLabelFontSize;
        }

        @Value("${app.dendrogram.labels.axis.font-size}")
        public void setAxisFontSize(int axisFontSize) {
            Config.axisFontSize = axisFontSize;
        }

        @Value("${app.dendrogram.lines.width}")
        public void setLineWidth(int lineWidth) {
            Config.lineWidth = lineWidth;
        }

        @Value("${app.dendrogram.colors.line}")
        public void setLineColor(String lineColor) {
            Config.lineColor = lineColor;
        }

        @Value("${app.dendrogram.colors.grid}")
        public void setGridColor(String gridColor) {
            Config.gridColor = gridColor;
        }

        @Value("${app.dendrogram.colors.label}")
        public void setTextColor(String textColor) {
            Config.textColor = textColor;
        }

        @Value("${app.dendrogram.colors.axis}")
        public void setAxisColor(String axisColor) {
            Config.axisColor = axisColor;
        }

        @Value("${app.dendrogram.colors.background}")
        public void setBackgroundColor(String backgroundColor) {
            Config.backgroundColor = backgroundColor;
        }
    }
}
