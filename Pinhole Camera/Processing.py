# CS194-26 (CS294-26): Project 1 starter Python code

# these are just some suggested libraries
# instead of scikit-image you could use matplotlib and opencv to read, write, and display images

import numpy as np
import skimage as sk
import skimage.io as skio
from skimage.color import rgb2gray
from skimage import transform as tf
from skimage import exposure as exposure
from PIL import Image

threshold = 1

def image_prep(imname):
	"""Processes the image, saves the image and outputs the image to the screen"""

	im = skio.imread(imname)
 
	im = sk.img_as_float(im)

	gray_im = rgb2gray(im)
	height = im.shape[0]
	width = im.shape[1]

	result = []
	index = 0

	for row in gray_im:
		summ = np.square(np.sum(row))
		result.append((index, summ))
		index += 1		

	mean = np.mean((min(result, key = lambda tup : tup[1]), max(result, key = lambda tup : tup[1])))
	mean = mean / threshold
	result = [x for x in result if x[1] >= mean]
	x1, x2 = result[0][0], result[-1][0]
	


	result = []
	index = 0


	for column in range(width):
		summ = np.square(np.sum(gray_im[:, column]))
		result.append((index, summ))
		index += 1		

	mean = np.mean((min(result, key = lambda tup : tup[1]), max(result, key = lambda tup : tup[1])))
	mean = mean / threshold
	result = [x for x in result if x[1] > mean]
	y1, y2 = result[0][0], result[-1][0]

		
	
	im_out = im[x1: x2, y1 : y2]
	print(im_out.shape)
	im_out = tf.rotate(im_out, 180)
	im_out = exposure.rescale_intensity(im_out)
	im_out = exposure.equalize_hist(im_out)
	im_out = exposure.equalize_adapthist(im_out)
	skio.imsave('All_3_techniques_' + imname[:-3] + "jpg", im_out)
	#skio.imshow(im_out)
	#skio.show()

def main():
	# image_names = []
	#for filename in jpg_image_names:
	#    colorize_jpeg(filename)
	for i in range(1):
	#or filename in image_names:
		image_prep(str(i + 1) + ".jpg")
		print(str(i + 1) + ".jpg")
	
main()