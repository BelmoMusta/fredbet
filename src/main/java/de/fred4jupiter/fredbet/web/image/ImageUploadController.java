package de.fred4jupiter.fredbet.web.image;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import de.fred4jupiter.fredbet.repository.ImageGroupRepository;
import de.fred4jupiter.fredbet.service.ImageUploadService;
import de.fred4jupiter.fredbet.web.MessageUtil;

@Controller
@RequestMapping("/image")
public class ImageUploadController {

	private static final String REDIRECT_SHOW_PAGE = "redirect:/image/show";

	private static final Logger log = LoggerFactory.getLogger(ImageUploadController.class);

	@Autowired
	private ImageGroupRepository imageGroupRepository;

	@Autowired
	private ImageUploadService imageUploadService;

	@Autowired
	private MessageUtil messageUtil;

	@ModelAttribute("availableImages")
	public List<ImageCommand> availableImages() {
		return imageUploadService.fetchAllImages();
	}

	@ModelAttribute("availableImageGroups")
	public List<String> availableImageGroups() {
		return imageGroupRepository.findAll().stream().map(imageGroup -> imageGroup.getName()).sorted()
				.collect(Collectors.toList());
	}

	@ModelAttribute("imageUploadCommand")
	public ImageUploadCommand initImageUploadCommand() {
		return new ImageUploadCommand();
	}

	@RequestMapping(value = "/show", method = RequestMethod.GET)
	public ModelAndView showUploadPage() {
		return new ModelAndView("image/imageUpload");
	}

	@RequestMapping(value = "/upload", method = RequestMethod.POST)
	public ModelAndView importParse(ImageUploadCommand imageUploadCommand, BindingResult result,
			RedirectAttributes redirect) {
		try {
			MultipartFile myFile = imageUploadCommand.getMyFile();
			if (myFile == null || myFile.getBytes() == null || myFile.getBytes().length == 0) {
				messageUtil.addErrorMsg(redirect, "image.upload.msg.noFileGiven");
				return new ModelAndView(REDIRECT_SHOW_PAGE);
			}

			if (!myFile.getContentType().equals(MediaType.IMAGE_JPEG_VALUE)) {
				messageUtil.addErrorMsg(redirect, "image.upload.msg.noJpegImage");
				return new ModelAndView(REDIRECT_SHOW_PAGE);
			}

			imageUploadService.saveImageInDatabase(myFile.getOriginalFilename(), myFile.getBytes(),
					imageUploadCommand.getGalleryGroup(), imageUploadCommand.getDescription(),
					imageUploadCommand.getRotation());
			messageUtil.addInfoMsg(redirect, "image.upload.msg.saved");
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			messageUtil.addErrorMsg(redirect, "image.upload.msg.failed");
		}

		return new ModelAndView(REDIRECT_SHOW_PAGE);
	}

	@RequestMapping(value = "/delete/{id}", method = RequestMethod.GET)
	public ModelAndView deleteImage(@PathVariable("id") Long imageId, RedirectAttributes redirect) {
		imageUploadService.deleteImageById(imageId);

		messageUtil.addInfoMsg(redirect, "image.gallery.msg.deleted");

		return new ModelAndView(REDIRECT_SHOW_PAGE);
	}
}
