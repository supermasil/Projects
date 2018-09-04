import colorize as colorize

def main():
	image_names = ['cathedral.jpg', 'monastery.jpg', 'nativity.jpg', 'settlers.jpg',
					'village.tif', 'harvesters.tif', 'train.tif', 'self_portrait.tif', 'three_generations.tif',
					'turkmen.tif', 'lady.tif', 'icon.tif', 'emir.tif', 'chasovnia.tif', 'ostrechiny.tif']
	
	#for filename in jpg_image_names:
	#    colorize_jpeg(filename)
		
	for filename in image_names:
		colorize.colorize(filename)
	
main()