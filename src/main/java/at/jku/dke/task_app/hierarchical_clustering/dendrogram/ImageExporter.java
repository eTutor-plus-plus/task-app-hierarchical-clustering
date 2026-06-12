package at.jku.dke.task_app.hierarchical_clustering.dendrogram;

import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Class for transforming strings in SVG format to other image formats.
 * <p>
 * Currently supports only PNG format, but can be easily expanded using
 * {@linkplain ImageFormat} to define a new format.
 */
public class ImageExporter {

    /**
     * Enum that represents different image formats.
     * <p>
     * Needs to define an {@link ImageTranscoder} from the
     * Apache Batik library as a ways of transforming from SVG to the
     * desired image format. Formats other than
     * PNG implemented by this library are JPG/JPEG and TIFF. Therefore,
     * if other formats are required, a custom ImageTranscoder needs
     * to be implemented or the signature of this enum must be changed.
     */
    public enum ImageFormat {
        PNG(new PNGTranscoder());

        private final ImageTranscoder transcoder;

        /**
         * Constructs a {@linkplain ImageFormat} with the transcoder for transforming.
         *
         * @param transcoder The transcoder for the image format.
         */
        ImageFormat(ImageTranscoder transcoder) {
            this.transcoder = transcoder;
        }
    }

    /**
     * Converts the given SVG content into an image in the specified format.
     *
     * @param format The target image format.
     * @param svg    The SVG content to be transformed.
     * @return The image in the specified format as a byte array.
     * @throws Exception If the conversion fails, e.g. due to invalid SVG content.
     */
    public byte[] export(ImageFormat format, String svg) throws Exception {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            export(format, svg, out);
            return out.toByteArray();
        }
    }

    /**
     * Performs the actual SVG-to-image conversion and writes the result to the given
     * {@link OutputStream}.
     * <p>
     * It uses the {@link ImageTranscoder} associated with the specified {@linkplain ImageFormat}
     * to transform the SVG content.
     *
     * @param format The target image format.
     * @param svg    The SVG content to be transformed.
     * @param out    The {@link OutputStream}.
     * @throws Exception If the conversion fails, e.g. due to invalid SVG content.
     */
    private void export(ImageFormat format, String svg, OutputStream out) throws Exception {
        ImageTranscoder transcoder = format.transcoder;

        byte[] svgBytes = svg.getBytes(StandardCharsets.UTF_8);
        TranscoderInput input = new TranscoderInput(new ByteArrayInputStream(svgBytes));
        TranscoderOutput output = new TranscoderOutput(out);

        transcoder.transcode(input, output);
    }

}
