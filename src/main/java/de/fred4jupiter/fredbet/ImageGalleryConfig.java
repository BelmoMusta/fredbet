package de.fred4jupiter.fredbet;

import de.fred4jupiter.fredbet.props.FredbetProperties;
import de.fred4jupiter.fredbet.repository.ImageBinaryRepository;
import de.fred4jupiter.fredbet.service.image.storage.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto configures the beans in repect to their configuration in the config
 * properties.
 *
 * @author michael
 */
@Configuration
public class ImageGalleryConfig {

    private static final String IMAGE_LOCATION = "image-location";

    private static final Logger LOG = LoggerFactory.getLogger(ImageGalleryConfig.class);

    @ConditionalOnProperty(prefix = FredbetProperties.PROPS_PREFIX, name = IMAGE_LOCATION, havingValue = "FILE_SYSTEM", matchIfMissing = true)
    @Bean
    public ImageLocationStrategy filesystemImageLocationService(FredbetProperties fredbetProperties) {
        return new FilesystemImageLocationStrategy(fredbetProperties.getImageFileSystemBaseFolder());
    }

    @ConditionalOnProperty(prefix = FredbetProperties.PROPS_PREFIX, name = IMAGE_LOCATION, havingValue = "DATABASE", matchIfMissing = false)
    @Bean
    public ImageLocationStrategy databaseImageLocationService(ImageBinaryRepository imageBinaryRepository) {
        LOG.info("Storing images in database.");
        return new DatabaseImageLocationStrategy(imageBinaryRepository);
    }

    @ConditionalOnProperty(prefix = FredbetProperties.PROPS_PREFIX, name = IMAGE_LOCATION, havingValue = "AWS_S3", matchIfMissing = false)
    @Bean
    public ImageLocationStrategy awsS3ImageLocationStrategy(AmazonS3ClientWrapper amazonS3ClientWrapper) {
        LOG.info("Storing images in AWS S3.");
        return new AwsS3ImageLocationStrategy(amazonS3ClientWrapper);
    }

    @ConditionalOnProperty(prefix = FredbetProperties.PROPS_PREFIX, name = IMAGE_LOCATION, havingValue = "AWS_S3", matchIfMissing = false)
    @Bean
    public AmazonS3ClientWrapper amazonS3ClientWrapper(FredbetProperties fredbetProperties) {
        return new AmazonS3ClientWrapper(fredbetProperties);
    }
}
