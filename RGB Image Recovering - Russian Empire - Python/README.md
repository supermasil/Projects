colorize.py contains methods to find the best alignment between the RGB channels and combine them into a final color image

Colorize(imname) will split the image in to RGB channel, crop them and call the appropriate methods to find the alignment, save the final image and show it to the screen

Ssd(image1, image2) will find the ssd between two images

Shift(image, displacements) will shift the image as specified in displacements

Crop_center(image, rangee) will crop the image at the center by 2 * rangee px of width and height

Crop_edge(image, rangee) will crop the edges of the image by rangee px

Offset(image1, image2, rangee) will find the displacements between two images by search within range [-rangee, rangee]

Pyramid_offSet(image1, image2, level) will recursively search for displacements at different levels



main.py will run colorize(imname) on all the images
