import matplotlib.pyplot as plt
from scipy import ndimage
from scipy import sparse
from scipy import misc
import numpy as np
from numpy import linalg as LA
import skimage as sk
import skimage.io as skio
from sklearn import preprocessing
import copy

import cv2 as cv
import numpy as np,sys

from align_image_code import align_images

### 1.1 sharpening
def sharpen(im, k):
	smoothed = ndimage.gaussian_filter(im, k)
	detail = np.clip(np.subtract(im, smoothed), 0, 1)
	return np.clip(np.add(im, detail), 0, 1)

### 1.1 file read
# fname = "./1_1/karl_headshot.png"
# im = misc.imread(fname, flatten = False)/255.
# sharpened_im = sharpen(im, k = 100)
# misc.imsave("1_1/karl_sharpened100.png", sharpened_im)

def low_pass(im, sigma):
	return ndimage.gaussian_filter(im, sigma)

def high_pass(im, sigma):
	low_pass_im = low_pass(im, sigma)
	return np.clip(np.subtract(im,low_pass_im), 0, 1)

def hybrid_image(im1, im2, name1, name2, sigma1, sigma2):

	plt.imsave("1.2/" + name1 + "_original.png", np.log(np.abs(np.fft.fftshift(np.fft.fft2(im1)))))
	plt.imsave("1.2/" + name2 + "_original.png", np.log(np.abs(np.fft.fftshift(np.fft.fft2(im2)))))
	im2_aligned, im1_aligned = align_images(im2, im1)
	low_pass_im = low_pass(im1_aligned, sigma1)
	plt.imsave("1.2/" + name1 + "_lowpass.png", np.log(np.abs(np.fft.fftshift(np.fft.fft2(low_pass_im)))))
	high_pass_im = high_pass(im2_aligned, sigma2)
	plt.imsave("1.2/" + name2 + "_highpass.png", np.log(np.abs(np.fft.fftshift(np.fft.fft2(high_pass_im)))))

	return low_pass_im + high_pass_im

def rgb2gray(rgb):
    return np.dot(rgb[...,:3], [0.299, 0.587, 0.114])

def stacks(hybrid, name, N):
	gaus_stack = []
	prev_gaus = copy.copy(hybrid)
	lap_stack = []
	lap_image = copy.copy(hybrid)
	for i in range(0, N):
		gaus_image = low_pass(prev_gaus, 2 ** i)
		lap_image = np.clip(np.subtract(prev_gaus,gaus_image), 0, 1)
		if gaus_image.min() < 0:
			gaus_image -= gaus_image.min()
		gaus_image/= gaus_image.max()
		gaus_stack.append(gaus_image)
		#skio.imsave("1.3/" + name + "_gauss_" + str(i) + ".png", gaus_image)

		prev_gaus = gaus_image
		if lap_image.min() < 0:
			lap_image -= lap_image.min()
		lap_image/= lap_image.max()
		lap_stack.append(lap_image)
		#skio.imsave("1.3/" + name + "_lap_" + str(i) + ".png", lap_image)

	return lap_stack, gaus_stack


# ONLY WORKS WITH PICS OF SIZE OF MULTIPLE OF 2
# INSPIRED BY OPENCV
def multi_res_blend(im1, im2, N):
	# generate Gaussian pramid for A
	copy1 = im1.copy()
	gaussA = [copy1]
	for i in range(N + 1):
	    copy1 = cv.pyrDown(copy1)
	    gaussA.append(copy1)
	# generate Gaussian pyramid for B

	copy1 = im2.copy()
	gaussB = [copy1]
	for i in range(N):
	    copy1 = cv.pyrDown(copy1)
	    gaussB.append(copy1)

	# generate Laplacian Pyramid for A
	lapA = [gaussA[N]]
	for i in range(N,0,-1):
	    temp = cv.pyrUp(gaussA[i])
	    temp1 = gaussA[i-1] - temp
	    lapA.append(temp1)
	# generate Laplacian Pyramid for B
	lapB = [gaussB[N]]
	for i in range(N,0,-1):
	    temp = cv.pyrUp(gaussB[i])
	    temp1 = gaussB[i-1] - temp
	    lapB.append(temp1)
	# Now add left and right halves of images in each level
	lapStack = []
	for la,lb in zip(lapA,lapB):
	    rows,cols,dpt = la.shape
	    ls = np.hstack((la[:,0:int(cols/2)], lb[:,int(cols/2):]))
	    lapStack.append(ls)
	# now reconstruct

	firstLevel = lapStack[0]
	for i in range(1,N+1):
	    firstLevel = cv.pyrUp(firstLevel)
	    firstLevel = firstLevel + lapStack[i]
	# image with direct connecting each half
	real = np.hstack((im1[:,:int(cols/2)],im2[:,int(cols/2):]))
	return firstLevel, real

def main():
	# 1.1
	# im = misc.imread("eminem.jpg", flatten = False)/255.
	# for i in range(12)[::2]:
	# 	sharpened_im = sharpen(im, i)
	# 	misc.imsave("1.1/eminem_sharpened_k=" + str(i) + ".png", sharpened_im)

	# 1.2
	# name1 = "michael"
	# name2 = "prince"
	# im1 = misc.imread(name1 + ".jpg")/255.
	# im2 = misc.imread(name2 + ".jpg")/255
	# sigma1 = 2
	# sigma2 = 15

	# hybrid = hybrid_image(im1, im2, name1, name2, sigma1, sigma2)
	# plt.imsave("1.2/" + name1 + "_" + name2 + "_final_" + str(sigma1) + "_" + str(sigma2) + ".png", hybrid)
	
	# 1.3
	# N = 3
	# name1 = "mona"
	# name2 = "dali"

	# im1 = misc.imread(name1 + ".jpg")/255.
	# im2 = misc.imread(name2 + ".jpg")/255

	# lap_stack1, gaus_stack1 = stacks(im1, name1, N)
	# lap_stack2, gaus_stack2 = stacks(im2, name2, N)
		
	# Redo part 1.2 with stacks
	# name1 = "nutmeg"
	# name2 = "derek"

	# im1 = misc.imread(name1 + ".jpg")/255.
	# im2 = misc.imread(name2 + ".jpg")/255

	# im1_aligned, im2_aligned = align_images(im1, im2)

	# lap_stack1, gaus_stack1 = stacks(im1_aligned, name1, N)
	# lap_stack2, gaus_stack2 = stacks(im2_aligned, name2, N)
	
	# im = gaus_stack2[0]/N + lap_stack1[0]/N

	# for i in range(1, len(gaus_stack1)):
	# 	im += gaus_stack2[i]/N + lap_stack1[i]/N

	# plt.imsave("1.3/" + name1 + "_" + name2 + "_using stack_N = " + str(N) + ".png", im)

	

	# 1.4
	# Size has to be power of 2
	print("here")

main()
