package es.unizar.webeng.lab3

import com.ninjasquad.springmockk.MockkBean
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import io.mockk.every
import io.mockk.justRun
import io.mockk.verify
import java.util.Optional

private val MANAGER_REQUEST_BODY = { name: String ->
    """
    { 
        "role": "Manager", 
        "name": "$name" 
    }
    """
}

private val MANAGER_RESPONSE_BODY = { name: String, id: Int ->
    """
    { 
       "name" : "$name",
       "role" : "Manager",
       "id" : $id
    }
    """
}

@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
@AutoConfigureMockMvc
class ControllerTests {

    @Autowired
    private lateinit var mvc: MockMvc

    @MockkBean
    private lateinit var employeeRepository: EmployeeRepository

    @Test
    fun `POST is not safe and not idempotent`() {
        // En POST cada petición crea un nuevo recurso, por eso devuelve IDs distintos
        every { employeeRepository.save(any<Employee>()) } answers {
            Employee("Mary", "Manager", 1)
        } andThenAnswer {
            Employee("Mary", "Manager", 2)
        }

        mvc.post("/employees") {
            contentType = MediaType.APPLICATION_JSON
            content = MANAGER_REQUEST_BODY("Mary")
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isCreated() }
            header { string("Location", "http://localhost/employees/1") }
            content {
                    contentType(MediaType.APPLICATION_JSON)
                    json(MANAGER_RESPONSE_BODY("Mary", 1))
                }
        }

        mvc
            .post("/employees") {
                contentType = MediaType.APPLICATION_JSON
                content = MANAGER_REQUEST_BODY("Mary")
                accept = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isCreated() }
                header { string("Location", "http://localhost/employees/2") }
                content {
                    contentType(MediaType.APPLICATION_JSON)
                    json(MANAGER_RESPONSE_BODY("Mary", 2))
                }
            }

        // Se guarda dos veces, no hay otras llamadas
        verify(exactly = 2) { employeeRepository.save(any<Employee>()) }
        verify(exactly = 0) {
            employeeRepository.findById(any())
            employeeRepository.deleteById(any())
        }
    }

    @Test
    fun `GET is safe and idempotent`() {
        // GET solo consulta, sin modificar nada
        every { employeeRepository.findById(1) } returns Optional.of(Employee("Mary", "Manager", 1))
        every { employeeRepository.findById(2) } returns Optional.empty()

        mvc.get("/employees/1").andExpect {
            status { isOk() }
            content {
                contentType(MediaType.APPLICATION_JSON)
                json(MANAGER_RESPONSE_BODY("Mary", 1))
            }
        }

        mvc.get("/employees/1").andExpect {
            status { isOk() }
            content {
                contentType(MediaType.APPLICATION_JSON)
                json(MANAGER_RESPONSE_BODY("Mary", 1))
            }
        }

        mvc.get("/employees/2").andExpect {
            status { isNotFound() }
        }

        // Solo se llama findById, nada más
        verify(exactly = 2) { employeeRepository.findById(1) }
        verify(exactly = 1) { employeeRepository.findById(2) }
        verify(exactly = 0) {
            employeeRepository.save(any<Employee>())
            employeeRepository.deleteById(any())
        }
    }

    @Test
    fun `PUT is idempotent but not safe`() {
        // PUT modifica el estado pero hacerlo varias veces deja el mismo resultado
        every { employeeRepository.findById(1) } answers {
            Optional.empty()
        } andThenAnswer {
            Optional.of(Employee("Tom", "Manager", 1))
        }

        every { employeeRepository.save(any<Employee>()) } returns Employee("Tom", "Manager", 1)

        mvc
            .put("/employees/1") {
                contentType = MediaType.APPLICATION_JSON
                content = MANAGER_REQUEST_BODY("Tom")
                accept = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isCreated() }
                header { string("Content-Location", "http://localhost/employees/1") }
                content {
                    contentType(MediaType.APPLICATION_JSON)
                    json(MANAGER_RESPONSE_BODY("Tom", 1))
                }
            }

        mvc
            .put("/employees/1") {
                contentType = MediaType.APPLICATION_JSON
                content = MANAGER_REQUEST_BODY("Tom")
                accept = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isOk() }
                header { string("Content-Location", "http://localhost/employees/1") }
                content {
                    contentType(MediaType.APPLICATION_JSON)
                    json(MANAGER_RESPONSE_BODY("Tom", 1))
                }
            }

        // findById y save se ejecutan dos veces, no hay más llamadas
        verify(exactly = 2) { employeeRepository.findById(1) }
        verify(exactly = 2) { employeeRepository.save(any<Employee>()) }
    }

    @Test
    fun `DELETE is idempotent but not safe`() {
        // DELETE borra pero hacerlo dos veces no cambia el resultado
        justRun { employeeRepository.deleteById(1) }

        mvc.delete("/employees/1").andExpect {
            status { isNoContent() }
        }

        mvc.delete("/employees/1").andExpect {
            status { isNoContent() }
        }

        // Solo se borra, sin llamadas adicionales
        verify(exactly = 2) { employeeRepository.deleteById(1) }
        verify(exactly = 0) {
            employeeRepository.save(any<Employee>())
            employeeRepository.findById(any())
        }
    }
}
