package game.profile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Iterator;

//======================================================================================================================
// 이미지 파일 올리는데 필요한거 따로 빼둔 코드
//======================================================================================================================
public final class ProfileImageCodec {
    private static final int MAX_UPLOAD_BYTES = 1024 * 1024;
    private static final int MAX_DOWNLOAD_BYTES = 2 * 1024 * 1024;
    private static final int MAX_IMAGE_DIMENSION = 4096;
    private static final long MAX_IMAGE_PIXELS = 8_000_000L;

    private ProfileImageCodec() {
    }

    //정사각형으로 자르고, png로 줄이고 base64로 db에 저장하는거
    //이거도 ai 사용함
    public static String encodeForUpload(BufferedImage source) throws IOException {
        if (source == null || source.getWidth() <= 0 || source.getHeight() <= 0) {
            throw new IllegalArgumentException("지원하는 이미지 파일이 아닙니다.");
        }
        source = centerCropToSquare(source);

        double scale = Math.min(1.0, 512.0 / Math.max(source.getWidth(), source.getHeight()));
        for (int attempt = 0; attempt < 8; attempt++) {
            int width = Math.max(1, (int) Math.round(source.getWidth() * scale));
            int height = Math.max(1, (int) Math.round(source.getHeight() * scale));
            BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = resized.createGraphics();
            try {
                graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                graphics.drawImage(source, 0, 0, width, height, null);
            } finally {
                graphics.dispose();
            }

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            if (!ImageIO.write(resized, "png", output)) {
                throw new IOException("PNG 인코더를 사용할 수 없습니다.");
            }
            byte[] encoded = output.toByteArray();
            if (encoded.length <= MAX_UPLOAD_BYTES) return Base64.getEncoder().encodeToString(encoded);

            // 서버 제한보다 큰 이미지는 반복마다 크기를 줄여 다시 인코딩합니다.
            scale *= 0.75;
        }
        throw new IllegalArgumentException("이미지 용량이 너무 큽니다.");
    }

    //그냥 중앙 잘라버림, 가로세로 찌그러뜨리는방식이 아닙니다
    public static BufferedImage centerCropToSquare(Image source) {
        if (source == null) throw new IllegalArgumentException("지원하는 이미지 파일이 아닙니다.");
        int width = source.getWidth(null);
        int height = source.getHeight(null);
        if (width <= 0 || height <= 0) throw new IllegalArgumentException("이미지를 읽을 수 없습니다.");

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D imageGraphics = image.createGraphics();
        try {
            imageGraphics.drawImage(source, 0, 0, null);
        } finally {
            imageGraphics.dispose();
        }

        int side = Math.min(width, height);
        int x = (width - side) / 2;
        int y = (height - side) / 2;
        BufferedImage square = new BufferedImage(side, side, BufferedImage.TYPE_INT_ARGB);
        Graphics2D squareGraphics = square.createGraphics();
        try {
            squareGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            squareGraphics.drawImage(image, 0, 0, side, side, x, y, x + side, y + side, null);
        } finally {
            squareGraphics.dispose();
        }
        return square;
    }

    //파일 크기 검사
    public static byte[] decodeFromServer(String encodedImage) throws IOException {
        if (encodedImage == null || encodedImage.length() > (MAX_DOWNLOAD_BYTES * 4L / 3L) + 8) {
            throw new IllegalArgumentException("프로필 이미지 데이터가 너무 큽니다.");
        }
        byte[] imageBytes = Base64.getDecoder().decode(encodedImage);
        if (imageBytes.length == 0 || imageBytes.length > MAX_DOWNLOAD_BYTES) {
            throw new IllegalArgumentException("프로필 이미지 데이터가 너무 큽니다.");
        }

        try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(imageBytes))) {
            if (input == null) throw new IllegalArgumentException("프로필 이미지 형식을 확인할 수 없습니다.");
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) throw new IllegalArgumentException("지원하지 않는 프로필 이미지 형식입니다.");

            ImageReader reader = readers.next();
            BufferedImage decoded;
            int width;
            int height;
            try {
                reader.setInput(input, true, true);
                width = reader.getWidth(0);
                height = reader.getHeight(0);
                if (width <= 0 || height <= 0 || width > MAX_IMAGE_DIMENSION
                        || height > MAX_IMAGE_DIMENSION || (long) width * height > MAX_IMAGE_PIXELS) {
                    throw new IllegalArgumentException("프로필 이미지 해상도가 너무 큽니다.");
                }
                decoded = reader.read(0);
                if (decoded == null) throw new IllegalArgumentException("프로필 이미지가 손상되었습니다.");
            } finally {
                reader.dispose();
            }
            if (width != height) {
                BufferedImage square = centerCropToSquare(decoded);
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                if (!ImageIO.write(square, "png", output)) {
                    throw new IOException("정사각형 프로필 이미지를 만들 수 없습니다.");
                }
                return output.toByteArray();
            }
        }
        return imageBytes;
    }
}
