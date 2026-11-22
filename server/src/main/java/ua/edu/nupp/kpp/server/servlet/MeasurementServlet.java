package ua.edu.nupp.kpp.server.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import ua.edu.nupp.kpp.server.model.Measurement;
import ua.edu.nupp.kpp.server.service.MeasurementService;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

@WebServlet("/measurements")
public class MeasurementServlet extends HttpServlet {
    private static final Logger LOG = Logger.getLogger(MeasurementServlet.class.getName());
    private final MeasurementService service = new MeasurementService();
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
            if ("delete".equals(action)) {
                service.delete(Long.parseLong(req.getParameter("id")));
            } else if ("add".equals(action) || "update".equals(action)) {
                Measurement m = new Measurement(
                        "update".equals(action) ? Long.parseLong(req.getParameter("id")) : null,
                        Long.parseLong(req.getParameter("sampleId")),
                        req.getParameter("parameterName"),
                        Double.parseDouble(req.getParameter("measuredValue")),
                        Double.parseDouble(req.getParameter("normValue")),
                        req.getParameter("unit")
                );
                if ("add".equals(action)) {
                    service.add(m);
                } else {
                    service.update(m);
                }
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
