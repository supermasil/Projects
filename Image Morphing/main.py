import matplotlib
import matplotlib.pyplot as plt
import matplotlib.image as image
from scipy.spatial import Delaunay
from scipy.spatial import tsearch
from matplotlib.path import Path

from scipy import misc
import numpy as np
import ast
import copy
import time
import os

import stored_george_putin_points as A
import stored_justin_brad_points as B
import stored_george_mean_points as C

POINTS = 50 # ask for 50 points
FRAMES = 45 # Save 45 frames

def choose_points(image1, image2, filename):
	""" Ask for points from user input and save them to
		an output file """
	image1_points = correspondences(image1)
	image2_points = correspondences(image2)

	file = open("./stored_{}_points.py".format(filename), "w")
	file.write("image1_points = " + str(image1_points) + "\n")
	file.write("image2_points = " + str(image2_points) + "\n")
	file.close()
	return image1_points, image2_points

def correspondences(image1):
	""" Create a list of points specified by user input
		and the 4 corners of the image """
	plt.imshow(image1)
	i = 0
	image1_points = []
	while i < POINTS:
		x = plt.ginput(1, timeout = 0)
		image1_points.append([x[0][0], x[0][1]])
		plt.scatter(x[0][0], x[0][1])
		plt.draw()
		i += 1
	plt.close()

	image1_points.extend([[0,0], [0, image1.shape[0] - 1], [image1.shape[1] - 1, 0], [image1.shape[1] - 1, image1.shape[0] - 1]])
	return image1_points

def find_matrix(triangulations, image_points, mid_points):
	""" Compute an affine warp for each triangle in the trianglation
		from the original image into this new shape """
	matrix = []
	for triangle in triangulations.simplices:
		src = image_points[triangle, ]
		dest = mid_points[triangle, ]
		matrix.append(trangulate(src, dest))
	return matrix

def trangulate(triangle1, triangle2):
	""" Compute an affine transformation matrix A between two triangles """
	A = np.matrix(str(triangle1[0][0]) + " " + str(triangle1[0][1]) + " 1 0 0 0;"
				 +"0 0 0 " + str(triangle1[0][0]) + " " + str(triangle1[0][1]) + " 1;"
				 +str(triangle1[1][0]) + " " + str(triangle1[1][1]) + " 1 0 0 0;"
				 +"0 0 0 " + str(triangle1[1][0]) + " " + str(triangle1[1][1]) + " 1;"
				 +str(triangle1[2][0]) + " " + str(triangle1[2][1]) + " 1 0 0 0;"
				 +"0 0 0 " + str(triangle1[2][0]) + " " + str(triangle1[2][1]) + " 1")

	b = np.matrix(str(triangle2[0][0]) + " " + str(triangle2[0][1]) + " " + str(triangle2[1][0]) + " " + str(triangle2[1][1]) + " " + str(triangle2[2][0]) + " " + str(triangle2[2][1]))

	least_square = np.linalg.lstsq(A, np.transpose(b))[0]
	stacked = np.vstack((np.reshape(least_square, (2, 3)), [0, 0, 1]))

	return stacked

def morphing(image1, image2, image1_points, image2_points, name):
	""" Morphing the 2 images based on the  weights
		and save each of the generated frames"""
	weights = np.linspace(0.0, 1.0, FRAMES) # Generating the equally spaced weights from 0 to 1
	trianglesA = Delaunay(image1_points) # Triangulate over the points
	trianglesB = Delaunay(image2_points)
	
	for i in range(FRAMES):
		average_points = average_shape(image1_points, image2_points, weights[i]) # Find the average shape for the image
		triangulations = Delaunay(average_points) # Triangulate over the points in the average shape
		frame = mid_way_face(image1, image2, image1_points, image2_points, average_points, triangulations, weights[i])
		misc.imsave("./Frames/" + name + "_{}.jpg".format(i), frame)
		print(i)

def mid_way_face(image1, image2, image1_points, image2_points, imMid_points, triangulations, weight):
	""" Find the midway face between the two image where they are most blended """
	matrixA = find_matrix(triangulations, image1_points, imMid_points)
	matrixB = find_matrix(triangulations, image2_points, imMid_points)

	result = image1.copy()
	
	for y in range(image1.shape[0]):
		for x in range(image1.shape[1]):
			index = tsearch(triangulations, (x, y)) # Find the simplices containing the point
			# Compute the affined points from the two images
			pointA = np.dot(np.linalg.inv(matrixA[index]), [x, y, 1])
			pointB = np.dot(np.linalg.inv(matrixB[index]), [x, y, 1])
		
			# Extracting the affined points
			x1 = min(image1.shape[1] - 1, np.int(pointA[0, 0]))
			y1 = min(image1.shape[0] - 1, np.int(pointA[0, 1]))
			x2 = min(image2.shape[1] - 1 , np.int(pointB[0, 0]))
			y2 = min(image2.shape[0] - 1, np.int(pointB[0, 1]))
			# print(y, x, y1, x1, y2, x2)
			result[y, x, :] = image1[y1, x1, :] * weight + image2[y2, x2, :] * (1-weight)
	return result

def average_shape(image1_points, image2_points, weight):
	""" Calculate the average shape for two images by weighing
		the points of the corresponding images.
		Return an array containing the points
		corresponding to the shape of the new
		average image. """

	shape = []
	for index in range(len(image1_points)):
		point1 = image1_points[index]
		point2 = image2_points[index]
		x = weight * point1[0] + (1 - weight) * point2[0]
		y = weight * point1[1] + (1 - weight) * point2[1]
		shape.append([x, y])

	return np.array(shape)

def read_the_files(points_path, image_path):
	""" Read information from the files in Dane set """
	image = plt.imread(image_path)
	h = image.shape[0]
	w = image.shape[1]

	with open(points_path, 'r') as file:
		lines = file.readlines()
		lines = [l.split() for l in lines if l and l[0] in "0123456"]
		del lines[0], lines[len(lines) - 1]
		result = []
		for l in lines:
			x = np.int(w * float(l[2]))
			y = np.int(h * float(l[3]))
			result.append([x, y])
	result.extend([[0, 0], [w, 0], [0, h], [w, h]])
	return np.matrix(result)

def mean_face(dir):
	""" Find the mean face of a given population of images """
	face_directory = os.path.join(os.getcwd(), dir)
	extracted_points = []
	file_names = []
	for filename in os.listdir(face_directory):
		name = os.path.splitext(filename)
		if name[1] == ".asf":
			points_path = os.path.join(face_directory, filename)
			image_path = os.path.join(face_directory, name[0] + ".bmp")
			pointsMatrix = read_the_files(points_path, image_path)
			extracted_points.append(pointsMatrix)
			file_names.append(image_path)

	sum_all_the_points = np.array(sum(extracted_points))
	average_points = sum_all_the_points/len(extracted_points)
	file = open("./dane_points.py", "w")
	file.write("average_points = " + str(average_points.tolist()) + "\n")
	file.close()

	warping(file_names, extracted_points, average_points, "meanface.jpg")

def warping(im_names, image_points, average_points, saved_name, t = 1):
	""" Warp the faces into the avaerage shape """
	image_points = np.array(image_points)
	triangulation = Delaunay(average_points)
	image = plt.imread(im_names[0])
	h = image.shape[0]
	w = image.shape[1]

	result = np.zeros((h, w, 3), dtype="float32")
	for i in range(len(im_names)):
		image = plt.imread(im_names[i])
		resultMatrix = find_matrix(triangulation, image_points[i], average_points)
		for y in range(h):
			for x in range(w):
				tri_index = tsearch(triangulation, (x, y))
				point = np.dot(np.linalg.inv(resultMatrix[tri_index]), [x, y, 1])
		
				x1 = point[0, 0]
				y1 = point[0, 1]

				result[y, x, :] += image[np.int(y1), np.int(x1), :]
	result = result/len(im_names)
	misc.imsave(saved_name, result)

def show_mesh(image, image_points):
	""" Show the triangulated image with the selected points by user """
	image_points = np.array(image_points)
	triangle = Delaunay(image_points)
	plt.triplot(image_points[:,0], image_points[:,1], triangle.simplices.copy())
	plt.plot(image_points[:, 0], image_points[:, 1], 'o')
	plt.imshow(image)
	plt.show()

def main():
	"""Morph the faces"""
	# image1 = plt.imread("george.jpg")
	# image2 = plt.imread("putin.jpg")

	# image1_points, image2_points = choose_points(image1, image2, "george_putin")
	# morphed_im = morphing(image1, image2, np.array(image1_points), np.array(image2_points), "george_putin")
	# show_mesh(image1, B.image1_points)
	# show_mesh(image2, B.image2_points)

	""" Find the mean face """
	#ean_face("data")


	""" Warp George to the mean face and vice versa """
	# image1 = plt.imread("george.jpg")
	# image2 = plt.imread("meanface_resized.jpg")
	# image1_points, image2_points = choose_points(image1, image2, "george_mean")

	## Uncomment this to retrieve the saved points
	## image1_points, image2_points = C.image1_points, C.image2_points

	# show_mesh(image1, C.image1_points) # George points
	# show_mesh(image2, C.image2_points) # Mean points

	# George to Mean
	# warping(["george.jpg"], [C.image1_points], np.array(C.image2_points), "george_to_mean.jpg")

	# Mean to George
	# warping(["meanface_resized.jpg"], [C.image2_points], np.array(C.image1_points), "mean_to_george.jpg")

	# import dane_points
	# average_points = dane_points.average_points
	# fileName = "./data/15-1f"
	# warping([fileName + ".bmp"], [np.array(read_the_files(fileName + ".asf", fileName + ".bmp"))], np.array(average_points), "15toAverage.jpg")

	""" Caricatures """
	# weight = -1 # Change this way to generate different effects
	## Uncomment this to retrieve the saved points
	## image1_points, image2_points = C.image1_points, C.image2_points
	# caricature = weight * (np.array(C.image1_points) - np.array(C.image2_points)) + np.array(C.image2_points)
	# warping(["george.jpg"], [C.image1_points], np.array(caricature), "george_carriatured" + "t=" + str(weight) + ".jpg")

	""" Bells And Whistles
		Change of gender, race, smile """
	# image1 = plt.imread("george.jpg")
	# image2 = plt.imread("michelle.jpg")

	# image1_points, image2_points = choose_points(image1, image2, "george_michelle")
	# morphed_im = morphing(image1, image2, np.array(image1_points), np.array(image2_points), "george_michelle")

main()
