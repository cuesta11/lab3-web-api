# Lab 3 Complete a Web API -- Project Report

## Description of Changes
Se han completado todos los bloques `SETUP` y `VERIFY` dentro del archivo `ControllerTests.kt` para los cuatro métodos principales del `EmployeeController`: `POST`, `GET`, `PUT` y `DELETE`.  
En cada uno se han configurado los mocks de `employeeRepository` para simular los distintos escenarios (empleado existente, no existente, creación o borrado).  

También se han añadido las verificaciones correspondientes (`verify`) para comprobar cuántas veces se invocan los métodos del repositorio, asegurando que se cumplen las propiedades de seguridad (safe) e idempotencia de cada método HTTP.  
Finalmente se han ejecutado los tests y todos han pasado correctamente.  

## Technical Decisions
Se ha decidido mantener la estructura original del profesor e ir modificando lo que indicaba:
- **POST**: se configuró el mock `save()` para devolver dos IDs distintos y demostrar que no es idempotente.  
- **GET**: se configuraron dos respuestas (`Optional.of` y `Optional.empty`) para simular casos exitosos y 404, verificando que no hay llamadas a métodos de modificación.  
- **PUT**: se encadenaron dos respuestas en `findById()` y `save()` para representar el caso de creación seguida de actualización, cumpliendo la propiedad de idempotencia.  
- **DELETE**: se utilizó `justRun` sobre `deleteById()` al tratarse de un método `void`, verificando que no afecta si se repite.  

## Learning Outcomes
Durante esta práctica he reforzado la comprensión del comportamiento de los métodos HTTP y sus propiedades:  
- Qué significa que una operación sea safe (sin modificar estado).  
- Qué implica que sea idempotente (repetirla no cambia el resultado).  
También he repasado el uso de MockK en tests de Spring Boot y cómo combinar respuestas encadenadas con `answers` y `andThenAnswer`.  

## AI Disclosure
### AI Tools Used
- ChatGPT (modelo GPT-5)

### AI-Assisted Work
La herramienta se utilizó de forma puntual para revisar el funcionamiento esperado de los métodos HTTP y proponer ejemplos de uso de MockK.  
Porcentaje aproximado de ayuda: **30%** (solo en apoyo y corrección de detalles).

### Original Work
Toda la integración de los tests, la verificación de los resultados y la redacción final fueron realizadas de forma manual tras entender el código base.  
He comprobado cada test y los he modificado hasta que todos pasaron correctamente, entendiendo el porqué de cada verificación.
