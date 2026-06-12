package at.jku.dke.task_app.hierarchical_clustering.dendrogram;

import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class ImageExporter {

    public enum ImageFormat {
        PNG(new PNGTranscoder());

        private final ImageTranscoder transcoder;

        ImageFormat(ImageTranscoder transcoder) {
            this.transcoder = transcoder;
        }
    }

    public byte[] export(ImageFormat format, String svg) throws Exception {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            export(format, svg, out);
            return out.toByteArray();
        }
    }

    private void export(ImageFormat format, String svg, OutputStream out) throws Exception {
        ImageTranscoder transcoder = format.transcoder;

        byte[] svgBytes = svg.getBytes(StandardCharsets.UTF_8);
        TranscoderInput input = new TranscoderInput(new ByteArrayInputStream(svgBytes));
        TranscoderOutput output = new TranscoderOutput(out);

        transcoder.transcode(input, output);
    }

}
