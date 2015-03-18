package com.sun.directory.examples;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;

import com.sun.DAO.ControlErrores;
import com.sun.businessLogic.validate.Emisor;
import com.tradise.reportes.util.key.GenericTransaction;
import com.util.util.key.Environment;
import com.util.util.key.Util;
import com.tradise.reportes.entidades.FacCabDocumento;
import com.tradise.reportes.servicios.ReporteSentencias;

public class ServiceDataNoEnviados extends GenericTransaction
{
	ArrayList<Emisor> Listcontenido;
	Emisor emite = null;
	public static String databaseType=null;
	InfoEmpresa infEmp = new InfoEmpresa();
	String ruc ="";
	
	Date dateFechaAutorizacion = null;
	int contadorAut = 0;
	int contadorNoAut = 0;
	String nameFile = "", file_adjunto = "";
	FacCabDocumento CabDoc=new FacCabDocumento();
	ReporteSentencias rpSen= new ReporteSentencias();
	
	private static org.apache.log4j.Logger logNoEnviados = null;
	
	public ServiceDataNoEnviados(int hilo, 
			   		   	 InfoEmpresa infoEmpresa, 
			   		   	 Emisor emitir, 
			   		   	 String nivelLog,
			   		   	 int intentos,
			   		   	 int timeIntentos)
	{
		
	}
	
	
	public ServiceDataNoEnviados(String ruc)
	{
		this.ruc = ruc;
	}
	
	
	public synchronized void atiendeHilo() throws Exception
	{
		emite = new Emisor();
		ServiceDataAutorizacion.databaseType = Util.driverConection;
				
		if (ServiceDataAutorizacion.databaseType.indexOf("postgresql")>0)
			ServiceDataAutorizacion.databaseType = "PostgreSQL";
		if (ServiceDataAutorizacion.databaseType.indexOf("sqlserver")>0)
			ServiceDataAutorizacion.databaseType = "SQLServer";
		infEmp = emite.obtieneInfoEmpresa(ruc);
		
		
		logNoEnviados = Logger.getLogger("Thread" + Thread.currentThread().getName());
		logNoEnviados.debug("---- No Enviados ----");
		
		while ((Environment.cf.readCtrl().equals("S")))
		{
			Thread.currentThread().sleep(60000);
			
			int limite = Integer.parseInt(Environment.c.getString("facElectronica.general.NOENVIADOS.limiteConsultaNoEnviados"));
			int minutos = Integer.parseInt(Environment.c.getString("facElectronica.general.NOENVIADOS.minutosEntreConsultas"));

			Listcontenido = emite.getTrxNoEnviados(ruc, "WS", minutos, limite);
		
			if (Listcontenido !=null)
			{
				if(Listcontenido.size()>0)
				{
					for (int i=0; i < Listcontenido.size(); i++)
					{
						try
						{
							String nameXml = Listcontenido.get(i).getAmbiente() +
								  		     ruc +
								  		     Listcontenido.get(i).getCodigoDocumento()+
								  		     Listcontenido.get(i).getCodEstablecimiento()+
								  		     Listcontenido.get(i).getCodPuntoEmision()+
								  		     Listcontenido.get(i).getSecuencial()+".xml";
						
							Listcontenido.get(i).setFilexml(nameXml);
							
							System.out.println("  No Enviados --> Paso a generados");
							File docNoEnviado;
							File docGen = new File(infEmp.getDirGenerado() + Listcontenido.get(i).getFilexml());
							
							if(!docGen.exists())
							{
								docNoEnviado = new File(infEmp.getDirRecibidos() + Listcontenido.get(i).getFilexml());
								if (docNoEnviado.exists())
								{
									ArchivoUtils.stringToArchivo(infEmp.getDirGenerado() + Listcontenido.get(i).getFilexml(),
						   					ArchivoUtils.archivoToString(infEmp.getDirRecibidos() + Listcontenido.get(i).getFilexml()));
									System.out.println("  No Enviados --> Pasado a generados");
								}
								else
								{
									docNoEnviado = new File(infEmp.getDirRecibidos() +"reenviados/" + Listcontenido.get(i).getFilexml());
									if (docNoEnviado.exists())
									{
										ArchivoUtils.stringToArchivo(infEmp.getDirGenerado() + Listcontenido.get(i).getFilexml(),
							   					ArchivoUtils.archivoToString(infEmp.getDirRecibidos() +"reenviados/" + Listcontenido.get(i).getFilexml()));
										System.out.println("  No Enviados --> Pasado a generados");
									}
								}
									
							}
						
						}catch(Exception excep)
						{
							System.out.println("  DataNoEnviados --> Error");
							System.out.println(excep);
				            System.out.println(excep.getMessage());
				            System.out.println(excep.getCause());
				        	System.out.println(excep.getLocalizedMessage());
				        	System.out.println(excep.getStackTrace());
							excep.printStackTrace();  
						}
					
						Thread.currentThread().sleep(500);
					}
				}
			}
		}

	}

	
	public void run()
	{
		try{
			atiendeHilo();	
		}catch(Exception e){
			e.printStackTrace();
		}finally{
		}
	}


}
