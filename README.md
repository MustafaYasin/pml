
# Practical Machine Learning: Assisting Visually Impaired People

This project uses the YOLOv5 model, optimized for edge devices, to assist visually impaired people in everyday scenarios through AI. It identifies common objects in a visually impaired person's daily walk using a select set of 17 classes, making it easier for them to navigate their surroundings.

## Prerequisites

You should have the following installed:

- Python and pip
- PyTorch

## Data Collection and Training

The YOLOv5 model comes with a variety of 80 classes. For our project, we only needed 17 specific classes. Not all of these classes were part of the default YOLOv5 model, so we collected new labeled images for these classes from the [Open Images Dataset V6](https://storage.googleapis.com/openimages/web/index.html). 

We used the [OIDv4_ToolKit](https://github.com/EscVM/OIDv4_ToolKit) to convert the labels to a format suitable for our use.

## Training

First, download the [YOLOv5 Repo from ultralytics](https://github.com/ultralytics/yolov5) and install the requirements using the `requirements.txt` file. 

Create or alter an existing `Dataset.yaml` file to specify the classes and their sample locations. We used the pretrained weights of the small YOLOv5 model for our training.

Use the following command for training:

```shell
python train.py --img 640 --batch 16 --epochs 300 --data yourDataset.yaml --weights yolov5s.pt
```

For more details on our training process and results, refer to the provided correlation matrix and learning curve graphics.

## App and Model Deployment

Our app is based on the [official PyTorch Object Detection Demo App](https://github.com/pytorch/android-demo-app/tree/master/ObjectDetection). This app includes the necessary post-processing for the YOLO-style object detector to deliver final results, such as non-maximum-suppression (NMS), which is required to purge proposals with lower confidence scores from the result set as long as they overlap ones with higher scores to a certain threshold. To determine this threshold, the intersection over union (IoU) has to be computed. More details can be found [here](https://towardsdatascience.com/non-maximum-suppression-nms-93ce178e177c).

We added functionality to estimate distances, which requires knowledge of the perceived focal length of the device. This parameter is hardware-dependent and was determined during development. For a wider range of devices, a comprehensive list or a calibration activity would be necessary.

Before deploying the trained model to a mobile device, convert it to a format optimized for the PyTorch mobile lite interpreter. Place the converted model in the assets folder of the Android Studio project and load it with the `LiteModuleLoader` from the PyTorch library.

## Contact

For more information or any queries, feel free to reach out to me at `hello@mustafayasin.com`.

## License

This project is licensed under `MIT license`.
