package ru.staffbots.webserver.servlets;

import ru.staffbots.database.journal.Journal;
import ru.staffbots.tools.devices.Device;
import ru.staffbots.tools.devices.Devices;
import ru.staffbots.tools.devices.drivers.RegularESP32Device;
import ru.staffbots.tools.devices.drivers.general.NetworkDevice;
import ru.staffbots.webserver.AccountService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// Обработка запроса ресурса
public class DeviceServlet extends BaseServlet {

    public DeviceServlet(AccountService accountService) {
        super(null, accountService);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        String address = request.getRemoteAddr();
        String name = request.getParameter("name");
        for (Device device: Devices.list)
            if (device.getName().equalsIgnoreCase(name))
                if (device instanceof NetworkDevice) {
                    ((NetworkDevice) device).setAddress(address);
                    response.setStatus(HttpServletResponse.SC_OK);
                    Journal.add("AnyMassage", name + " added!");
                    return;
                }
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        doGet(request, response);
    }

}
