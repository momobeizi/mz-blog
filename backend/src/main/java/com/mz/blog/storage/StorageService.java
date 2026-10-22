package com.mz.blog.storage;

import com.mz.blog.common.exception.BusinessException;
import com.mz.blog.content.media.CmsMedia;
import com.mz.blog.content.media.MediaMapper;
import com.mz.blog.common.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StorageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final DateTimeFormatter DIR_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM");

    private final MediaMapper mediaMapper;

    @Value("${app.storage.local.root}")
    private String root;

    @Value("${app.storage.local.public-prefix}")
    private String publicPrefix;

    public CmsMedia storeImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw BusinessException.badRequest("请选择要上传的图片");
        }
        String originalName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "image" : file.getOriginalFilename());
        String extension = extension(originalName);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw BusinessException.badRequest("仅支持 JPG、PNG、GIF、WEBP 图片");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw BusinessException.badRequest("文件类型不正确");
        }
        String dateDir = LocalDate.now().format(DIR_FORMAT);
        String filename = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        Path base = Path.of(root).toAbsolutePath().normalize();
        Path targetDir = base.resolve(dateDir);
        Path target = targetDir.resolve(filename);
        try {
            Files.createDirectories(targetDir);
            try (InputStream input = file.getInputStream()) {
                Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
            }
            String sha256 = sha256(target);
            String url = publicPrefix + "/" + dateDir + "/" + filename;
            String thumbUrl = createThumbnail(target, dateDir, filename, extension);
            CmsMedia media = new CmsMedia();
            media.setOriginalName(originalName);
            media.setStorageKey(dateDir + "/" + filename);
            media.setUrl(url);
            media.setThumbUrl(thumbUrl == null ? url : thumbUrl);
            media.setMimeType(contentType);
            media.setExtension(extension);
            media.setSizeBytes(Files.size(target));
            media.setSha256(sha256);
            try {
                BufferedImage image = ImageIO.read(target.toFile());
                if (image != null) {
                    media.setWidth(image.getWidth());
                    media.setHeight(image.getHeight());
                }
            } catch (Exception ignored) {
                // Some formats (for example WEBP) may not have an ImageIO reader installed.
            }
            media.setCreatedBy(SecurityUtils.currentUserId());
            mediaMapper.insert(media);
            return media;
        } catch (IOException ex) {
            throw new BusinessException(50010, org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "文件保存失败");
        }
    }

    private String createThumbnail(Path source, String dateDir, String filename, String extension) {
        if ("gif".equals(extension)) {
            return null;
        }
        String thumbName = filename.substring(0, filename.lastIndexOf('.')) + "-thumb.jpg";
        Path target = source.getParent().resolve(thumbName);
        try {
            Thumbnails.of(source.toFile()).size(640, 480).outputFormat("jpg").outputQuality(0.82).toFile(target.toFile());
            return publicPrefix + "/" + dateDir + "/" + thumbName;
        } catch (Exception ignored) {
            return null;
        }
    }

    private String extension(String name) {
        int index = name.lastIndexOf('.');
        return index < 0 ? "" : name.substring(index + 1).toLowerCase(Locale.ROOT);
    }

    private String sha256(Path path) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream input = Files.newInputStream(path)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = input.read(buffer)) > 0) {
                    digest.update(buffer, 0, read);
                }
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (Exception ex) {
            return null;
        }
    }
}
