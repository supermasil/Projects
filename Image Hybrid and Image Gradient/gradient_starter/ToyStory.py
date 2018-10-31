import matplotlib.pyplot as plt
from scipy import sparse
from scipy import misc
import numpy as np
from sklearn import preprocessing


def toy_story(im):


	h = len(im)
	w =len(im[0])

	# create im2var
	index_matrix = np.zeros((h, w))
	
	k = 0
	for x in range(h):
		for y in range(w):
			index_matrix[x][y] = k
			k = k + 1			

	sparse_matrix = sparse.lil_matrix(((h * (w - 1)) + (w * (h - 1)) + 1, h * w), dtype = np.float32)
	known_vector = np.zeros(((h * (w - 1)) + (w * (h - 1)) + 1, 1))

	#print(known_vector.shape)
	#print(sparse_matrix.shape)
	e = 0
	# Get the x-gradient
	for y in range(h):
		for x in range(w - 1):
			sparse_matrix[e,index_matrix[y][x + 1]] = 1
			sparse_matrix[e,index_matrix[y][x]] = -1
			known_vector[e] = im[y][x + 1] - im[y][x]
			e = e + 1

	# Get the y-gradient
	for y in range(h - 1):
		for x in range(w):
			sparse_matrix[e, index_matrix[y + 1][x]] = 1
			sparse_matrix[e, index_matrix[y][x]] = -1
			known_vector[e] = im[y + 1][x] - im[y][x]
			e = e + 1

	sparse_matrix[e, index_matrix[0][0]] = 1
	known_vector[e] = im[0][0]

	sparse_matrix = sparse.csr_matrix(sparse_matrix)
	return np.reshape(sparse.linalg.lsqr(sparse_matrix, known_vector)[0], (h, w))

def main():

	im_toy = plt.imread('toy_problem.png')/255.
	reconstructed_toystory = toy_story(im_toy)
	misc.imsave("2.1/reconstructed_toystory.png", reconstructed_toystory)

main()
