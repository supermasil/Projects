from scipy import misc
import numpy as np
import matplotlib.pyplot as plt
import math
import os


def averageImage(path, scale, radius, name):
	numImage = 1
	result = None

	# Search for the middle image
	for file in os.listdir(path):
		if "out_08_08" in file:
			result =  misc.imread(path + "/" + file)/255.
			break

	for file in os.listdir(path):
		orig, coord = offSet(file, scale)
		if "out_08_08" in file:
			continue
		# Only calcualte within the specified radius
		if math.sqrt((orig[0] - 8)**2 + (orig[1] - 8)**2) <= radius:
			print(numImage)
			source_image = misc.imread(path + "/" + file)/255.
			source_image = shiftImage(source_image, coord)
			result += source_image
			numImage += 1

	# Average out
	result = result/float(numImage)
	misc.imsave("{}.png".format(name), result)

def shiftImage(image, coors):
    result = np.zeros(image.shape)
    range1, range2 = [], []

    # Select the range for the image

    if coors[0] > 0:
        result[coors[0]::,::,::] = image[:-coors[0]:,::,::]
    elif coors[0] < 0:
    	result[:coors[0]:,::,::] = image[-coors[0]::,::,::]

    if coors[1] > 0:
    	result[::,coors[1]::,::] = image[::,:-coors[1]:,::]
    elif coors[1] < 0:
    	result[::,:coors[1]:,::] = image[::,-coors[1]::,::]

    if coors[2] > 0:
    	result[::,::,coors[2]::] = image[::,::,:-coors[2]:]
    elif coors[2] < 0:
    	result[::,::,:coors[2]:] = image[::,::,-coors[2]::]

    return result

def offSet(image_title, scale):
	# Analyzing the image title
	title = image_title.split("-")
	number = title[0].split("_")
	x = int(number[2])
	y = int(number[1])

	x_shift = x - 8
	y_shift = 8 - y
	return (x, y), (scale * y_shift, scale * x_shift, 0)


def main():
	## Part 1 - Refocusing
	## Same Radius, different scale

	# for i in range(0, 5):
	# 	averageImage("./chess_board", i, 25, "./chess_board_output/shifted_scale_{}".format(i))

	# print("chess_board DONE")

	# for i in range(0, 5):
	# 	averageImage("./comic", i, 25, "./comic_output/shifted_scale_{}".format(i))

	# print("commic DONE")

	# ## Part 2 - Aparture Adjustment
	# ## Same scale, different radius
	# for i in range(0, 10):	
	# 	averageImage("./lego", 2, i, "./lego_output/aperture_{}".format(i))

	# print("lego DONE")

	# for i in range(0, 10):
	# 	averageImage("./bunny", 2, i, "./bunny_output/aperture_{}".format(i))

	# print("bunny DONE")

	# print("Done processing")

main()
