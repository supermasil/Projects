# CS194-26 (CS294-26): Project 1 starter Python code

# these are just some suggested libraries
# instead of scikit-image you could use matplotlib and opencv to read, write, and display images

import numpy as np
import skimage as sk
import skimage.io as skio
from skimage.transform import rescale
from skimage.filters import roberts as edge_finding
import math


def colorize(imname):
	"""Processes the image, saves the image and outputs the image to the screen"""

	# read in the image
	im = skio.imread(imname)

	# convert to double (might want to do this later on to save memory)    
	im = sk.img_as_float(im)

	# compute the height of each part (just 1/3 of total)
	height = (int) (np.floor(im.shape[0] / 3.0))

	# separate color channels into 3 equal parts
	b = im[:height]
	g = im[height: 2*height]
	r = im[2*height: 3*height]


	if(imname[-3:] == 'jpg'):
		cropped_g = crop_edge(g, 20)
		cropped_r = crop_edge(r, 20)
		cropped_b = crop_edge(b, 20)
	else: 
		#(imname[-3] == 'tif'):
		cropped_g = crop_edge(g, 800)
		cropped_r = crop_edge(r, 800)
		cropped_b = crop_edge(b, 800)

	edged_g = edge_finding(cropped_g)
	edged_r = edge_finding(cropped_r)
	edged_b = edge_finding(cropped_b)

	displacements_gb = pyramid_offSet(edged_g, edged_b, 1)
	displacements_rb = pyramid_offSet(edged_r, edged_b, 1)

	rolled_gb = shift(g, displacements_gb)
	rolled_rb = shift(r, displacements_rb)

	im_out = np.dstack([rolled_rb, rolled_gb, b])

	print(imname)
	print("green - blue: " + str(displacements_gb[0]) + ", " + str(displacements_gb[1]))
	print("red - blue: " + str(displacements_rb[0]) + ", " + str(displacements_rb[1]))

	# save the image
	fname = 'out_fname.jpg'
	skio.imsave('output_' + imname[:-4] + '.jpg', im_out)

	# display the image
	skio.imshow(im_out)
	skio.show()


def ssd(image1, image2):
	"""Calculate the SSD between 2 images"""
	return np.sum(np.sum(np.square(image1 - image2)))

def shift(image, displacements):
	return np.roll(np.roll(image, displacements[0], 0), displacements[1], 1)

def stack(r, g, b):
	return np.dstack((r, g, b))

def crop_center(image, rangee):
	y,x = image.shape
	cropX = x//2 - rangee
	cropY = y//2 - rangee 
	return image[cropX : -cropX, cropY : -cropY]

def crop_edge(image, rangee):
	return image[rangee : -rangee, rangee : -rangee]

def offSet(image1, image2, rangee):
	displacements = [0, 0]
	minOffSet = ssd(image1, image2)

	for x in range(-rangee , rangee):
		rolled_x_image1 = np.roll(image1, x, 0)

		for y in range(-rangee , rangee):
			roll_xy_image1 = np.roll(rolled_x_image1, y, 1)
			diff = ssd(roll_xy_image1, image2)
			if diff < minOffSet:
				minOffSet = diff
				displacements= [x, y]

	return displacements


def pyramid_offSet(image1, image2, level):

	if(image1.size < 160000 or level == 5): # stop at level 5
		return offSet(image1, image2, 15)

	else:		
		displacements = pyramid_offSet(sk.transform.rescale(image1, 1 / 2), sk.transform.rescale(image2, 1 / 2), level + 1)
		displacements = np.multiply(displacements, 2) # scale the offset up

		rolled_image1 = shift(image1, displacements) # shift the image with the displacements we got
		cropped_image1 = crop_center(rolled_image1, 100) # ONLY NEED 200px SQUARE IN THE CENTER
		cropped_image2 = crop_center(image2, 100)

		displacements = np.add(displacements, offSet(cropped_image1, cropped_image2, 15)) # RECALCULATE THE DISPLACEMENTS

		return displacements