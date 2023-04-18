## Target Scan Application

#### File Structures

* **JupyterNotebook_scripts folder** contains the Jupyter Notebook files for the image processing algorithm (`imageProcessing.ipynb`) and graph generation (`dataAnalysis.ipynb`). They are not a part of the application, and you can use them to try out the image processing algorithm. As explained in the report, they are used to develop the algorithm and graphs, and have the same functionality as the python scripts used in the application. Third-party libraries `cv2, numpy, matplotlib, scipy` are needed to run the python codes, and you can see the output images in Jupyter Notebooks. 

  It also contains 5 target paper images for testing the image processing algorithm, located in `originImage` subfolder. Uncomment the desired image in `imageProcessing.ipynb` to select it, and you can also add new images in this subfolder. 

```
# fileName = "02023-01-27001.jpg"
# fileName = "02023-02-18005.jpg"
# fileName = "02023-02-23011.jpg"
fileName = "02023-03-29007.jpg"
# fileName = "02023-03-31012.jpg"
```

* **TargetScan folder** contains the complete Android Studio project of the application. Using Android Studio to open it and you will get the same project.
* **TargetScan.apk** is the installation package of the application, which can be installed on Android phones with Android version 8.0 or later. The detail about the application are explained in the report.