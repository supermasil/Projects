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



def cal_v(hei, wid, mask):
	i = 0;
	for y in range(hei):
		for x in range(wid):
			if (ma_che(y, x, mask)):
				i = i + 1
	return i

def ma_che(y, x, mask):
	return mask[y][x] == 1

def cal_ad(x, y, hei, wid, mask, A , b, e, ivar, source, source_i, target):
	if ma_che(y, x, mask):
		A[e, ivar[y][x]] = -1
		b[e] = source_i - source[y][x]
	else:
		b[e] = target[y][x]

def cal_a_s(x, y, hei, wid, mask, A , b, e, ivar, source, target):
	#calc up
	cal_ad(x, y + 1, hei, wid, mask, A, b, e, ivar, source, source[y][x], target)
	A[e, ivar[y][x]] = 1
	e = e + 1
	#calc down
	cal_ad(x, y - 1, hei, wid, mask, A, b, e, ivar, source, source[y][x], target)
	A[e, ivar[y][x]] = 1
	e = e + 1
	#calc_right
	cal_ad(x + 1, y, hei, wid, mask, A, b, e, ivar, source, source[y][x], target)
	A[e, ivar[y][x]] = 1
	e = e + 1
	#calc_left
	val = cal_ad(x - 1, y, hei, wid, mask, A, b, e, ivar, source, source[y][x], target)
	A[e, ivar[y][x]] = 1
	e = e + 1

	return e

def p_blend(source, target, mask):
	hei = len(source)
	wid = len(source[0])
	num_vars = cal_v(hei, wid, mask)
	ivar = np.zeros((hei, wid))

	k = 0
	for y in range(hei):
		for x in range(wid):
			if ma_che(y, x, mask):
				ivar[y][x] = k	
				k = k + 1
			else:
				ivar[y][x] = -1

	A = sparse.lil_matrix(((num_vars * 4), num_vars), dtype = np.float32)
	b = np.zeros(((num_vars * 4), 1))
	e = 0

	for y in range(hei):
		for x in range(wid):
			if ma_che(y, x, mask):
				e = cal_a_s(x, y, hei, wid, mask, A , b, e, ivar, source, target)		
			
	A = sparse.csr_matrix(A)

	result = sparse.linalg.lsqr(A, b)[0]
	# if result.min() < 0:
	# 	result -= result.min()
	# result /= result.max()
	return np.clip(sparse.linalg.lsqr(A, b)[0], 0, 1)

def p_replace(result_vec, source, target, mask):
	k = 0
	for y in range(len(source)):
		for x in range(len(source[0])):
			if ma_che(y, x, mask):
				target[y][x] = result_vec[k]
				k = k + 1
	return target

def g_blend(source, target, mask):
	result_vec = p_blend(source, target, mask)
	target = p_replace(result_vec, source, target, mask)

	return target

def s_RGB(im):
	red = im[:,:,2]
	green = im[:,:,1]
	blue = im[:,:,0]
	return red, green, blue

def c_blend(source, target, mask):
	r_source, g_source, b_source = s_RGB(source)
	r_target, g_target, b_target = s_RGB(target)

	r_result = p_blend(r_source, r_target, mask)
	g_result = p_blend(g_source, g_target, mask)
	b_result = p_blend(b_source, b_target, mask)

	p_replace(r_result, r_source, r_target, mask)
	p_replace(g_result, g_source, g_target, mask)
	p_replace(b_result, b_source, b_target, mask)

	return np.dstack([b_target, g_target, r_target])

def main():
	# source = misc.imread('michaelp.jpg')/255.
	# # misc.imsave("source_image_gray.png", s)
	# target = misc.imread('prince.jpg')/255.
	# # misc.imsave("target_image_gray.png", t)
	# mask = misc.imread('michael_mask.jpg', flatten = True)/255.

	# ### grayscale images only
	# #result = g_blend(source, target, mask)

	# ### color images only
	# result = c_blend(source, target, mask)
	print("here")
	# misc.imsave("2.2/michael_prince.png", result)

main()
