# TCP Java Multithread Server
## Prerequisitos
### Crear Archivos
Para la ejecucion del codigo es necesario tener los archivos de parte del servidor que se desean enviar.
Los archivos se guardan en una carpeta `assets`

Se buca tener dos archivos uno de 100MB y otro de 250MB (Aunque el servidor funciona con cualquier tamaño de no mas 500MB)

Las instrucciones para crear archivos dummmy son las siguientes
#### Windows
  `fsutil file createnew <file> <size in bytes>`
##### Ejemplo:
  Un Archivo de 100MB con nombre f1
  
   `fsutil file createnew f1 104857600`
#### Linux
  `dd if=/dev/zero of=<file> bs=1MB count=<size in MB>`
##### Ejemplo:
  Un Archivo de 100MB con combre f1.bin
  
   `dd if=/dev/zero of=f1.bin bs=1MB count=100`

### Definir direccion ip
  Para la direccion ip es necesario conocer la direccion del servidor que envia los archivos.
  Esta direccion debe colocarse del lado del cliente 
  ```
  //Cliente
  private static final String SERVIDOR = <direccion>;`
  ```
  Para conocer la direccion en Windows y en algunas distribuciones de Linux, se puede usar este comando.
#### Windows
  `ipconfig`
#### Linux
  `ifconfig`
## Ejecucion
### Servidor y Cliente
  El programa se debe compilar en diferentes equipos uno con la funcion de servidor y otro con la funcion de cliente
#### Cliente

#### Servidor
