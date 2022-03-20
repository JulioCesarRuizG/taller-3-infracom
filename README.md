# TCP Java Multithread Server
## Prerrequisitos
Clonar el repositorio en la máquina donde se ejecutará el cliente (Windows) y donde se ejecutará el servidor (Linux).

### Crear Archivos
Para la ejecucion del coóigo es necesario tener los archivos de parte del servidor que se desean enviar.
Los archivos se guardan en una carpeta 'Servidor' dentro de una carpeta 'assets' en la raíz del proyecto.

Dentro de assets/Servidor se busca tener dos archivos uno de 100MiB (llamado f1) y otro de 250MiB (llamado f2). Aunque el servidor funciona con cualquier tamaño de no más 500MiB.

Las instrucciones para crear archivos dummmy son las siguientes
#### Windows
  `fsutil file createnew <file> <size in bytes>`
##### Ejemplo:
  Un Archivo de 100MB con nombre f1
  
   `fsutil file createnew f1 104857600`
#### Linux
  `dd if=/dev/zero of=<file> bs=1MiB count=<size in MiB>`
##### Ejemplo:
  Un Archivo de 100MiB con combre f1.bin
  
   `dd if=/dev/zero of=f1.bin bs=1MiB count=100`

### Definir direccion ip
  Para la direccion ip es necesario conocer la direccion del servidor que envía los archivos.
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
  
## Ejecución
### Servidor y Cliente
  El programa se debe compilar en diferentes equipos uno con la función de servidor y otro con la función de cliente
  
#### Servidor
En la raíz del proyecto ejecutar el siguiente comando para compilarlo:
sudo javac -cp src src/Main.java
En la raíz del proyecto ejecutar el siguiente comando para ejecutarlo:
sudo java -cp src src/Main.java

1. Ingresar el tipo de servicio (servidor)
2. Ingresar la cantidad de clientes (misma cantidad indicada en el cliente)
3. Ingresar el tamaño del archivo (100 o 250)

#### Cliente
1. Ingresar el tipo de servicio (cliente)
2. Ingresar la cantidad de clientes

