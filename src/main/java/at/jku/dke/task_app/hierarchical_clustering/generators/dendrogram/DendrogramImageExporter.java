package at.jku.dke.task_app.hierarchical_clustering.generators.dendrogram;

import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class DendrogramImageExporter {

    public byte[] export(String svg) throws Exception {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            export(svg, baos);
            return baos.toByteArray();
        }
    }

    private void export(String svg, OutputStream out) throws Exception {
        ImageTranscoder transcoder = new PNGTranscoder();

        byte[] svgBytes = svg.getBytes(StandardCharsets.UTF_8);
        TranscoderInput input = new TranscoderInput(new ByteArrayInputStream(svgBytes));
        TranscoderOutput output = new TranscoderOutput(out);

        transcoder.transcode(input, output);
    }

}
