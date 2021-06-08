# Project - QRCode Generator - November 2019
Note that this project was carried out as part of a first
year course at the Ecole Polytechnique Fédérale de Lausanne in Switzerland. However the project was minimal and many 
improvements were made. This QRCode generator can now generate QRCodes of all existing versions (1 to 40 instead of 1 
to 4). It can also encode them with the 4 possible correction levels (L, M, Q, H instead of just L). A small graphical 
interface has also been added.

## How to use it 
In the `main` method of the` Main` class you have the choice between two methods. 

The first launches an interface in which you can generate QRCodes by choosing the content, the level of correction, 
and the version. Note that if you choose version 0, the program will itself choose the most suitable version to 
generate your QRCode.

The second method generates a QRCode from the text contained in the `INPUT` variable with the correction level 
contained in the `LVL` variable. These two variables are declared as public, static, and final at the start of the 
`Main` class :
```java
public static final String INPUT = "Hello World !"
public static final char LVL = 'L';
```
You just have to choose which method you want to use by commenting the one you don't want and uncommenting the one you
want :
```java
new TextFieldInterface();
// generateQRCodeFromInput();
```
or
```java
// new TextFieldInterface();
generateQRCodeFromInput();
```

### Corrections Levels
The correction level determines the encoding used to encode the INPUT. In practice, a higher correction level may 
encode a shorter character string than a low correction level for the same size of QRCode. On the other hand, the 
QRCode encoded with a high level of correction will be less sensitive to the case: it will work even if a small part of
the QRCode is unreadable. Here are the 4 levels of corrections and the percentage of error they allow :

* `L` : 7 %
* `M` : 15 %
* `Q` : 25 %
* `H` : 30 %

In this QRCode generator, the correction level is determined by a character ('L', 'M', 'Q' or 'H'). If it's none of 
these 4, or if the letter isn't in capital, it'll be the correction `L` by default.

## Author
* **Jean-Baptiste Moreau**

***
