package at.jku.dke.task_app.hierarchical_clustering.generators.dendrogram;

import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class DendrogramImageExporter {

    public byte[] export(String format, String svg) throws Exception {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            export(format, svg, out);
            return out.toByteArray();
        }
    }

    private void export(String format, String svg, OutputStream out) throws Exception {
        ImageTranscoder transcoder = switch (format.toLowerCase()) {
            case "png" -> new PNGTranscoder();
            case "jpeg", "jpg" -> new JPEGTranscoder();
            default -> throw new IllegalArgumentException("Image format " + format.toLowerCase() + " is not supported.");
        };

        byte[] svgBytes = svg.getBytes(StandardCharsets.UTF_8);
        TranscoderInput input = new TranscoderInput(new ByteArrayInputStream(svgBytes));
        TranscoderOutput output = new TranscoderOutput(out);

        transcoder.transcode(input, output);
    }

}
