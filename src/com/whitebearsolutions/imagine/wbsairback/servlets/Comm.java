package com.whitebearsolutions.imagine.wbsairback.servlets;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.whitebearsolutions.imagine.wbsairback.net.CommResponse;

public class Comm extends HttpServlet {
	public final static long serialVersionUID = 91383535;
	public final static String baseUrl = "/admin/"+Comm.class.getSimpleName();
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		handle(request, response);
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		handle(request, response);
	}
	
	public void handle(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {	    	
	    	CommResponse _cr = new CommResponse(request, response);
	    	_cr.process();
		} catch (Exception _ex) {
			 PrintWriter _out = response.getWriter();
	    	_out.println(_ex.toString());
	    	_out.flush();
	    }
	}
}
