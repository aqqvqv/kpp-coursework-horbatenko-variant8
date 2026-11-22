package ua.edu.nupp.kpp.server.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import ua.edu.nupp.kpp.server.model.SampleState;
import ua.edu.nupp.kpp.server.service.SampleService;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

@WebServlet("/samples")
public class SampleServlet extends HttpServlet {
    private static final Logger LOG = Logger.getLogger(SampleServlet.class.getName());
    private final SampleService service = new SampleService();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String action = req.getParameter("action");
            if ("delete".equals(action)) {
                service.delete(Long.parseLong(req.getParameter("id")));
            }
            writeJson(resp, service.getAll());
        } catch (Exception e) {
            writeError(resp, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        try {
            String action = req.getParameter("action");
            if ("add".equals(action)) {
                service.add(
                        Long.parseLong(req.getParameter("pointId")),
                        req.getParameter("sampleDate")
                );
            } else if ("update".equals(action)) {
                service.update(
                        Long.parseLong(req.getParameter("id")),
                        Long.parseLong(req.getParameter("pointId")),
                        req.getParameter("sampleDate")
                );
            } else if ("transition".equals(action)) {
                String stateVal = req.getParameter("state");
                if (stateVal == null || stateVal.isBlank()) {
                    throw new IllegalArgumentException("Не вказано цільовий стан проби");
                }
                service.changeState(
                        Long.parseLong(req.getParameter("id")),
                        SampleState.valueOf(stateVal)
                );
            } else if ("delete".equals(action)) {
                service.delete(Long.parseLong(req.getParameter("id")));
            } else {
                throw new IllegalArgumentException("Невідома операція: " + action);
            }
            writeJson(resp, service.getAll());
        } catch (Exception e) {
            writeError(resp, e);
        }
    }

    private void writeJson(HttpServletResponse resp, Object data) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        mapper.writeValue(resp.getWriter(), data);
    }

    private void writeError(HttpServletResponse resp, Exception e) throws IOException {
        boolean clientError = e instanceof IllegalArgumentException;
        if (!clientError) LOG.severe("Помилка сервера: " + e.getMessage());
        resp.setStatus(clientError ? HttpServletResponse.SC_BAD_REQUEST : HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        writeJson(resp, Map.of("error", clientError ? e.getMessage() : "Внутрішня помилка сервера"));
    }
}
