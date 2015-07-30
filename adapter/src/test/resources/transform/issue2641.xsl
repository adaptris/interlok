<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:user="http://www.altova.com/MapForce/UDF/user" xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="user xs  user">
	<xsl:output method="xml" encoding="UTF-8" indent="yes"/>
	<xsl:template match="root">
		<xsl:call-template name="Ezflux"/>
	</xsl:template>
	<xsl:template name="Ezflux">
		<Envelope>
			<EnvelopeHeader>
				<SchemaVersion>4.2.0</SchemaVersion>
				<EnvelopeCreated>
					<xsl:value-of select="'20070711 00:00:00'"/>
					<!--<xsl:call-template name="f_dt">
						<xsl:with-param name="val" select=""/>
					</xsl:call-template>-->
				</EnvelopeCreated>
				<EnvelopeTrackingID>
					<xsl:value-of select="segment_Detail[1]/record_/ref_echantillon"/>
				</EnvelopeTrackingID>
				<EnvelopeRevisionNumber>1</EnvelopeRevisionNumber>
				<SourcePartnerID>GRS</SourcePartnerID>
				<SourceDivisionID>GRS</SourceDivisionID>
				<DestinationPartnerID>GON</DestinationPartnerID>
				<DestinationDivisionID>GON</DestinationDivisionID>
				<TestIndicator>False</TestIndicator>
			</EnvelopeHeader>
			<xsl:for-each select="segment_Detail/record_">
				<QualityDocument DocumentSequenceNumber="0" QualityDocumentType="Laboratory Report" MessageLifecycle="New">
					<DocumentHeader Currency="EUR" Language="FR">
						<DocumentReference AssignedBy="Buyer" Type="Sample Number">
							<xsl:value-of select="ref_echantillon"/>
						</DocumentReference>
						<DocumentReference AssignedBy="Laboratory" Type="Sample Number">
							<xsl:value-of select="10000*number(dossier) + number(echantillon)"/>
						</DocumentReference>
						<!--<DocumentDate Type="Sample Arrived Date">
							<xsl:call-template name="f_dt">
								<xsl:with-param name="val" select=""/>
							</xsl:call-template>
						</DocumentDate>-->
						<Organisation Type="Laboratory">
							<Reference AssignedBy="Laboratory" Type="Organisation ID">GRS</Reference>
						</Organisation>
						<Components>
							<Product>
								<Reference AssignedBy="Buyer" Type="Identifier"></Reference>
							</Product>
						</Components>
					</DocumentHeader>
					<xsl:if test="string-length(normalize-space(critere1))">
						<DocumentLine>
							<Criteria>
								<CriteriaID AssignedBy="Laboratory">
									<xsl:value-of select="/root/segment_Header/record_/critere1"/>
								</CriteriaID>
							</Criteria>
							<AnalysisResult>
								<xsl:element name="NumericResult">
									<xsl:attribute name="Type">Actual</xsl:attribute>
									<xsl:attribute name="Basis">Measure</xsl:attribute>
									<xsl:value-of select="critere1"/>
								</xsl:element>
							</AnalysisResult>
						</DocumentLine>
					</xsl:if>
					<xsl:if test="string-length(normalize-space(critere2))">
						<DocumentLine>
							<Criteria>
								<CriteriaID AssignedBy="Laboratory">
									<xsl:value-of select="/root/segment_Header/record_/critere2"/>
								</CriteriaID>
							</Criteria>
							<AnalysisResult>
								<xsl:element name="NumericResult">
									<xsl:attribute name="Type">Actual</xsl:attribute>
									<xsl:attribute name="Basis">Measure</xsl:attribute>
									<xsl:value-of select="critere2"/>
								</xsl:element>
							</AnalysisResult>
						</DocumentLine>
					</xsl:if>
					<xsl:if test="string-length(normalize-space(critere3))">
						<DocumentLine>
							<Criteria>
								<CriteriaID AssignedBy="Laboratory">
									<xsl:value-of select="/root/segment_Header/record_/critere3"/>
								</CriteriaID>
							</Criteria>
							<AnalysisResult>
								<xsl:element name="NumericResult">
									<xsl:attribute name="Type">Actual</xsl:attribute>
									<xsl:attribute name="Basis">Measure</xsl:attribute>
									<xsl:value-of select="critere3"/>
								</xsl:element>
							</AnalysisResult>
						</DocumentLine>
					</xsl:if>
					<xsl:if test="string-length(normalize-space(critere4))">
						<DocumentLine>
							<Criteria>
								<CriteriaID AssignedBy="Laboratory">
									<xsl:value-of select="/root/segment_Header/record_/critere4"/>
								</CriteriaID>
							</Criteria>
							<AnalysisResult>
								<xsl:element name="NumericResult">
									<xsl:attribute name="Type">Actual</xsl:attribute>
									<xsl:attribute name="Basis">Measure</xsl:attribute>
									<xsl:value-of select="critere4"/>
								</xsl:element>
							</AnalysisResult>
						</DocumentLine>
					</xsl:if>
					<xsl:if test="string-length(normalize-space(critere5))">
						<DocumentLine>
							<Criteria>
								<CriteriaID AssignedBy="Laboratory">
									<xsl:value-of select="/root/segment_Header/record_/critere5"/>
								</CriteriaID>
							</Criteria>
							<AnalysisResult>
								<xsl:element name="NumericResult">
									<xsl:attribute name="Type">Actual</xsl:attribute>
									<xsl:attribute name="Basis">Measure</xsl:attribute>
									<xsl:value-of select="critere5"/>
								</xsl:element>
							</AnalysisResult>
						</DocumentLine>
					</xsl:if>
					<xsl:if test="string-length(normalize-space(critere6))">
						<DocumentLine>
							<Criteria>
								<CriteriaID AssignedBy="Laboratory">
									<xsl:value-of select="/root/segment_Header/record_/critere6"/>
								</CriteriaID>
							</Criteria>
							<AnalysisResult>
								<xsl:element name="NumericResult">
									<xsl:attribute name="Type">Actual</xsl:attribute>
									<xsl:attribute name="Basis">Measure</xsl:attribute>
									<xsl:value-of select="critere6"/>
								</xsl:element>
							</AnalysisResult>
						</DocumentLine>
					</xsl:if>
					<xsl:if test="string-length(normalize-space(critere7))">
						<DocumentLine>
							<Criteria>
								<CriteriaID AssignedBy="Laboratory">
									<xsl:value-of select="/root/segment_Header/record_/critere7"/>
								</CriteriaID>
							</Criteria>
							<AnalysisResult>
								<xsl:element name="NumericResult">
									<xsl:attribute name="Type">Actual</xsl:attribute>
									<xsl:attribute name="Basis">Measure</xsl:attribute>
									<xsl:value-of select="critere7"/>
								</xsl:element>
							</AnalysisResult>
						</DocumentLine>
					</xsl:if>
					<xsl:if test="string-length(normalize-space(critere8))">
						<DocumentLine>
							<Criteria>
								<CriteriaID AssignedBy="Laboratory">
									<xsl:value-of select="/root/segment_Header/record_/critere8"/>
								</CriteriaID>
							</Criteria>
							<AnalysisResult>
								<xsl:element name="NumericResult">
									<xsl:attribute name="Type">Actual</xsl:attribute>
									<xsl:attribute name="Basis">Measure</xsl:attribute>
									<xsl:value-of select="critere8"/>
								</xsl:element>
							</AnalysisResult>
						</DocumentLine>
					</xsl:if>
					<xsl:if test="string-length(normalize-space(critere9))">
						<DocumentLine>
							<Criteria>
								<CriteriaID AssignedBy="Laboratory">
									<xsl:value-of select="/root/segment_Header/record_/critere9"/>
								</CriteriaID>
							</Criteria>
							<AnalysisResult>
								<xsl:element name="NumericResult">
									<xsl:attribute name="Type">Actual</xsl:attribute>
									<xsl:attribute name="Basis">Measure</xsl:attribute>
									<xsl:value-of select="critere9"/>
								</xsl:element>
							</AnalysisResult>
						</DocumentLine>
					</xsl:if>
					<xsl:if test="string-length(normalize-space(critere10))">
						<DocumentLine>
							<Criteria>
								<CriteriaID AssignedBy="Laboratory">
									<xsl:value-of select="/root/segment_Header/record_/critere10"/>
								</CriteriaID>
							</Criteria>
							<AnalysisResult>
								<xsl:element name="NumericResult">
									<xsl:attribute name="Type">Actual</xsl:attribute>
									<xsl:attribute name="Basis">Measure</xsl:attribute>
									<xsl:value-of select="critere10"/>
								</xsl:element>
							</AnalysisResult>
						</DocumentLine>
					</xsl:if>
					<xsl:if test="string-length(normalize-space(critere11))">
						<DocumentLine>
							<Criteria>
								<CriteriaID AssignedBy="Laboratory">
									<xsl:value-of select="/root/segment_Header/record_/critere11"/>
								</CriteriaID>
							</Criteria>
							<AnalysisResult>
								<xsl:element name="NumericResult">
									<xsl:attribute name="Type">Actual</xsl:attribute>
									<xsl:attribute name="Basis">Measure</xsl:attribute>
									<xsl:value-of select="critere11"/>
								</xsl:element>
							</AnalysisResult>
						</DocumentLine>
					</xsl:if>
					<xsl:if test="string-length(normalize-space(critere12))">
						<DocumentLine>
							<Criteria>
								<CriteriaID AssignedBy="Laboratory">
									<xsl:value-of select="/root/segment_Header/record_/critere12"/>
								</CriteriaID>
							</Criteria>
							<AnalysisResult>
								<xsl:element name="NumericResult">
									<xsl:attribute name="Type">Actual</xsl:attribute>
									<xsl:attribute name="Basis">Measure</xsl:attribute>
									<xsl:value-of select="critere12"/>
								</xsl:element>
							</AnalysisResult>
						</DocumentLine>
					</xsl:if>
					<xsl:if test="string-length(normalize-space(critere13))">
						<DocumentLine>
							<Criteria>
								<CriteriaID AssignedBy="Laboratory">
									<xsl:value-of select="/root/segment_Header/record_/critere13"/>
								</CriteriaID>
							</Criteria>
							<AnalysisResult>
								<xsl:element name="NumericResult">
									<xsl:attribute name="Type">Actual</xsl:attribute>
									<xsl:attribute name="Basis">Measure</xsl:attribute>
									<xsl:value-of select="critere13"/>
								</xsl:element>
							</AnalysisResult>
						</DocumentLine>
					</xsl:if>
					<xsl:if test="string-length(normalize-space(critere14))">
						<DocumentLine>
							<Criteria>
								<CriteriaID AssignedBy="Laboratory">
									<xsl:value-of select="/root/segment_Header/record_/critere14"/>
								</CriteriaID>
							</Criteria>
							<AnalysisResult>
								<xsl:element name="NumericResult">
									<xsl:attribute name="Type">Actual</xsl:attribute>
									<xsl:attribute name="Basis">Measure</xsl:attribute>
									<xsl:value-of select="critere14"/>
								</xsl:element>
							</AnalysisResult>
						</DocumentLine>
					</xsl:if>
					<xsl:if test="string-length(normalize-space(critere15))">
						<DocumentLine>
							<Criteria>
								<CriteriaID AssignedBy="Laboratory">
									<xsl:value-of select="/root/segment_Header/record_/critere15"/>
								</CriteriaID>
							</Criteria>
							<AnalysisResult>
								<xsl:element name="NumericResult">
									<xsl:attribute name="Type">Actual</xsl:attribute>
									<xsl:attribute name="Basis">Measure</xsl:attribute>
									<xsl:value-of select="critere15"/>
								</xsl:element>
							</AnalysisResult>
						</DocumentLine>
					</xsl:if>
					<xsl:if test="string-length(normalize-space(critere16))">
						<DocumentLine>
							<Criteria>
								<CriteriaID AssignedBy="Laboratory">
									<xsl:value-of select="/root/segment_Header/record_/critere16"/>
								</CriteriaID>
							</Criteria>
							<AnalysisResult>
								<xsl:element name="NumericResult">
									<xsl:attribute name="Type">Actual</xsl:attribute>
									<xsl:attribute name="Basis">Measure</xsl:attribute>
									<xsl:value-of select="critere16"/>
								</xsl:element>
							</AnalysisResult>
						</DocumentLine>
					</xsl:if>
					<xsl:if test="string-length(normalize-space(critere17))">
						<DocumentLine>
							<Criteria>
								<CriteriaID AssignedBy="Laboratory">
									<xsl:value-of select="/root/segment_Header/record_/critere17"/>
								</CriteriaID>
							</Criteria>
							<AnalysisResult>
								<xsl:element name="NumericResult">
									<xsl:attribute name="Type">Actual</xsl:attribute>
									<xsl:attribute name="Basis">Measure</xsl:attribute>
									<xsl:value-of select="critere17"/>
								</xsl:element>
							</AnalysisResult>
						</DocumentLine>
					</xsl:if>
					<xsl:if test="string-length(normalize-space(critere18))">
						<DocumentLine>
							<Criteria>
								<CriteriaID AssignedBy="Laboratory">
									<xsl:value-of select="/root/segment_Header/record_/critere18"/>
								</CriteriaID>
							</Criteria>
							<AnalysisResult>
								<xsl:element name="NumericResult">
									<xsl:attribute name="Type">Actual</xsl:attribute>
									<xsl:attribute name="Basis">Measure</xsl:attribute>
									<xsl:value-of select="critere18"/>
								</xsl:element>
							</AnalysisResult>
						</DocumentLine>
					</xsl:if>

					<xsl:if test="string-length(normalize-space(critere19))">
						<DocumentLine>
							<Criteria>
								<CriteriaID AssignedBy="Laboratory">
									<xsl:value-of select="/root/segment_Header/record_/critere19"/>
								</CriteriaID>
							</Criteria>
							<AnalysisResult>
								<xsl:element name="NumericResult">
									<xsl:attribute name="Type">Actual</xsl:attribute>
									<xsl:attribute name="Basis">Measure</xsl:attribute>
									<xsl:value-of select="critere19"/>
								</xsl:element>
							</AnalysisResult>
						</DocumentLine>
					</xsl:if>
					<xsl:if test="string-length(normalize-space(critere20))">
						<DocumentLine>
							<Criteria>
								<CriteriaID AssignedBy="Laboratory">
									<xsl:value-of select="/root/segment_Header/record_/critere20"/>
								</CriteriaID>
							</Criteria>
							<AnalysisResult>
								<xsl:element name="NumericResult">
									<xsl:attribute name="Type">Actual</xsl:attribute>
									<xsl:attribute name="Basis">Measure</xsl:attribute>
									<xsl:value-of select="critere20"/>
								</xsl:element>
							</AnalysisResult>
						</DocumentLine>
					</xsl:if>
					<xsl:if test="string-length(normalize-space(critere21))">
						<DocumentLine>
							<Criteria>
								<CriteriaID AssignedBy="Laboratory">
									<xsl:value-of select="/root/segment_Header/record_/critere21"/>
								</CriteriaID>
							</Criteria>
							<AnalysisResult>
								<xsl:element name="NumericResult">
									<xsl:attribute name="Type">Actual</xsl:attribute>
									<xsl:attribute name="Basis">Measure</xsl:attribute>
									<xsl:value-of select="critere21"/>
								</xsl:element>
							</AnalysisResult>
						</DocumentLine>
					</xsl:if>
					<xsl:if test="string-length(normalize-space(critere22))">
						<DocumentLine>
							<Criteria>
								<CriteriaID AssignedBy="Laboratory">
									<xsl:value-of select="/root/segment_Header/record_/critere22"/>
								</CriteriaID>
							</Criteria>
							<AnalysisResult>
								<xsl:element name="NumericResult">
									<xsl:attribute name="Type">Actual</xsl:attribute>
									<xsl:attribute name="Basis">Measure</xsl:attribute>
									<xsl:value-of select="critere22"/>
								</xsl:element>
							</AnalysisResult>
						</DocumentLine>
					</xsl:if>
					<xsl:if test="string-length(normalize-space(critere23))">
						<DocumentLine>
							<Criteria>
								<CriteriaID AssignedBy="Laboratory">
									<xsl:value-of select="/root/segment_Header/record_/critere23"/>
								</CriteriaID>
							</Criteria>
							<AnalysisResult>
								<xsl:element name="NumericResult">
									<xsl:attribute name="Type">Actual</xsl:attribute>
									<xsl:attribute name="Basis">Measure</xsl:attribute>
									<xsl:value-of select="critere23"/>
								</xsl:element>
							</AnalysisResult>
						</DocumentLine>
					</xsl:if>
					<xsl:if test="string-length(normalize-space(critere24))">
						<DocumentLine>
							<Criteria>
								<CriteriaID AssignedBy="Laboratory">
									<xsl:value-of select="/root/segment_Header/record_/critere24"/>
								</CriteriaID>
							</Criteria>
							<AnalysisResult>
								<xsl:element name="NumericResult">
									<xsl:attribute name="Type">Actual</xsl:attribute>
									<xsl:attribute name="Basis">Measure</xsl:attribute>
									<xsl:value-of select="critere24"/>
								</xsl:element>
							</AnalysisResult>
						</DocumentLine>
					</xsl:if>
					<xsl:if test="string-length(normalize-space(critere25))">
						<DocumentLine>
							<Criteria>
								<CriteriaID AssignedBy="Laboratory">
									<xsl:value-of select="/root/segment_Header/record_/critere25"/>
								</CriteriaID>
							</Criteria>
							<AnalysisResult>
								<xsl:element name="NumericResult">
									<xsl:attribute name="Type">Actual</xsl:attribute>
									<xsl:attribute name="Basis">Measure</xsl:attribute>
									<xsl:value-of select="critere25"/>
								</xsl:element>
							</AnalysisResult>
						</DocumentLine>
					</xsl:if>
					<xsl:if test="string-length(normalize-space(critere26))">
						<DocumentLine>
							<Criteria>
								<CriteriaID AssignedBy="Laboratory">
									<xsl:value-of select="/root/segment_Header/record_/critere26"/>
								</CriteriaID>
							</Criteria>
							<AnalysisResult>
								<xsl:element name="NumericResult">
									<xsl:attribute name="Type">Actual</xsl:attribute>
									<xsl:attribute name="Basis">Measure</xsl:attribute>
									<xsl:value-of select="critere26"/>
								</xsl:element>
							</AnalysisResult>
						</DocumentLine>
					</xsl:if>
					<xsl:if test="string-length(normalize-space(critere27))">
						<DocumentLine>
							<Criteria>
								<CriteriaID AssignedBy="Laboratory">
									<xsl:value-of select="/root/segment_Header/record_/critere27"/>
								</CriteriaID>
							</Criteria>
							<AnalysisResult>
								<xsl:element name="NumericResult">
									<xsl:attribute name="Type">Actual</xsl:attribute>
									<xsl:attribute name="Basis">Measure</xsl:attribute>
									<xsl:value-of select="critere27"/>
								</xsl:element>
							</AnalysisResult>
						</DocumentLine>
					</xsl:if>
					<xsl:if test="string-length(normalize-space(critere28))">
						<DocumentLine>
							<Criteria>
								<CriteriaID AssignedBy="Laboratory">
									<xsl:value-of select="/root/segment_Header/record_/critere28"/>
								</CriteriaID>
							</Criteria>
							<AnalysisResult>
								<xsl:element name="NumericResult">
									<xsl:attribute name="Type">Actual</xsl:attribute>
									<xsl:attribute name="Basis">Measure</xsl:attribute>
									<xsl:value-of select="critere28"/>
								</xsl:element>
							</AnalysisResult>
						</DocumentLine>
					</xsl:if>
					<xsl:if test="string-length(normalize-space(critere29))">
						<DocumentLine>
							<Criteria>
								<CriteriaID AssignedBy="Laboratory">
									<xsl:value-of select="/root/segment_Header/record_/critere29"/>
								</CriteriaID>
							</Criteria>
							<AnalysisResult>
								<xsl:element name="NumericResult">
									<xsl:attribute name="Type">Actual</xsl:attribute>
									<xsl:attribute name="Basis">Measure</xsl:attribute>
									<xsl:value-of select="critere29"/>
								</xsl:element>
							</AnalysisResult>
						</DocumentLine>
					</xsl:if>
					<xsl:if test="string-length(normalize-space(critere30))">
						<DocumentLine>
							<Criteria>
								<CriteriaID AssignedBy="Laboratory">
									<xsl:value-of select="/root/segment_Header/record_/critere30"/>
								</CriteriaID>
							</Criteria>
							<AnalysisResult>
								<xsl:element name="NumericResult">
									<xsl:attribute name="Type">Actual</xsl:attribute>
									<xsl:attribute name="Basis">Measure</xsl:attribute>
									<xsl:value-of select="critere30"/>
								</xsl:element>
							</AnalysisResult>
						</DocumentLine>
					</xsl:if>
					<xsl:if test="string-length(normalize-space(critere31))">
						<DocumentLine>
							<Criteria>
								<CriteriaID AssignedBy="Laboratory">
									<xsl:value-of select="/root/segment_Header/record_/critere31"/>
								</CriteriaID>
							</Criteria>
							<AnalysisResult>
								<xsl:element name="NumericResult">
									<xsl:attribute name="Type">Actual</xsl:attribute>
									<xsl:attribute name="Basis">Measure</xsl:attribute>
									<xsl:value-of select="critere31"/>
								</xsl:element>
							</AnalysisResult>
						</DocumentLine>
					</xsl:if>
					<xsl:if test="string-length(normalize-space(critere32))">
						<DocumentLine>
							<Criteria>
								<CriteriaID AssignedBy="Laboratory">
									<xsl:value-of select="/root/segment_Header/record_/critere32"/>
								</CriteriaID>
							</Criteria>
							<AnalysisResult>
								<xsl:element name="NumericResult">
									<xsl:attribute name="Type">Actual</xsl:attribute>
									<xsl:attribute name="Basis">Measure</xsl:attribute>
									<xsl:value-of select="critere32"/>
								</xsl:element>
							</AnalysisResult>
						</DocumentLine>
					</xsl:if>
					<xsl:if test="string-length(normalize-space(critere33))">
						<DocumentLine>
							<Criteria>
								<CriteriaID AssignedBy="Laboratory">
									<xsl:value-of select="/root/segment_Header/record_/critere33"/>
								</CriteriaID>
							</Criteria>
							<AnalysisResult>
								<xsl:element name="NumericResult">
									<xsl:attribute name="Type">Actual</xsl:attribute>
									<xsl:attribute name="Basis">Measure</xsl:attribute>
									<xsl:value-of select="critere33"/>
								</xsl:element>
							</AnalysisResult>
						</DocumentLine>
					</xsl:if>
					<xsl:if test="string-length(normalize-space(critere34))">
						<DocumentLine>
							<Criteria>
								<CriteriaID AssignedBy="Laboratory">
									<xsl:value-of select="/root/segment_Header/record_/critere34"/>
								</CriteriaID>
							</Criteria>
							<AnalysisResult>
								<xsl:element name="NumericResult">
									<xsl:attribute name="Type">Actual</xsl:attribute>
									<xsl:attribute name="Basis">Measure</xsl:attribute>
									<xsl:value-of select="critere34"/>
								</xsl:element>
							</AnalysisResult>
						</DocumentLine>
					</xsl:if>
					<xsl:if test="string-length(normalize-space(critere35))">
						<DocumentLine>
							<Criteria>
								<CriteriaID AssignedBy="Laboratory">
									<xsl:value-of select="/root/segment_Header/record_/critere35"/>
								</CriteriaID>
							</Criteria>
							<AnalysisResult>
								<xsl:element name="NumericResult">
									<xsl:attribute name="Type">Actual</xsl:attribute>
									<xsl:attribute name="Basis">Measure</xsl:attribute>
									<xsl:value-of select="critere35"/>
								</xsl:element>
							</AnalysisResult>
						</DocumentLine>
					</xsl:if>
					<xsl:if test="string-length(normalize-space(critere36))">
						<DocumentLine>
							<Criteria>
								<CriteriaID AssignedBy="Laboratory">
									<xsl:value-of select="/root/segment_Header/record_/critere36"/>
								</CriteriaID>
							</Criteria>
							<AnalysisResult>
								<xsl:element name="NumericResult">
									<xsl:attribute name="Type">Actual</xsl:attribute>
									<xsl:attribute name="Basis">Measure</xsl:attribute>
									<xsl:value-of select="critere36"/>
								</xsl:element>
							</AnalysisResult>
						</DocumentLine>
					</xsl:if>
					<xsl:if test="string-length(normalize-space(critere37))">
						<DocumentLine>
							<Criteria>
								<CriteriaID AssignedBy="Laboratory">
									<xsl:value-of select="/root/segment_Header/record_/critere37"/>
								</CriteriaID>
							</Criteria>
							<AnalysisResult>
								<xsl:element name="NumericResult">
									<xsl:attribute name="Type">Actual</xsl:attribute>
									<xsl:attribute name="Basis">Measure</xsl:attribute>
									<xsl:value-of select="critere37"/>
								</xsl:element>
							</AnalysisResult>
						</DocumentLine>
					</xsl:if>
					<xsl:if test="string-length(normalize-space(critere38))">
						<DocumentLine>
							<Criteria>
								<CriteriaID AssignedBy="Laboratory">
									<xsl:value-of select="/root/segment_Header/record_/critere38"/>
								</CriteriaID>
							</Criteria>
							<AnalysisResult>
								<xsl:element name="NumericResult">
									<xsl:attribute name="Type">Actual</xsl:attribute>
									<xsl:attribute name="Basis">Measure</xsl:attribute>
									<xsl:value-of select="critere38"/>
								</xsl:element>
							</AnalysisResult>
						</DocumentLine>
					</xsl:if>
					<xsl:if test="string-length(normalize-space(critere39))">
						<DocumentLine>
							<Criteria>
								<CriteriaID AssignedBy="Laboratory">
									<xsl:value-of select="/root/segment_Header/record_/critere39"/>
								</CriteriaID>
							</Criteria>
							<AnalysisResult>
								<xsl:element name="NumericResult">
									<xsl:attribute name="Type">Actual</xsl:attribute>
									<xsl:attribute name="Basis">Measure</xsl:attribute>
									<xsl:value-of select="critere39"/>
								</xsl:element>
							</AnalysisResult>
						</DocumentLine>
					</xsl:if>
					<xsl:if test="string-length(normalize-space(critere40))">
						<DocumentLine>
							<Criteria>
								<CriteriaID AssignedBy="Laboratory">
									<xsl:value-of select="/root/segment_Header/record_/critere40"/>
								</CriteriaID>
							</Criteria>
							<AnalysisResult>
								<xsl:element name="NumericResult">
									<xsl:attribute name="Type">Actual</xsl:attribute>
									<xsl:attribute name="Basis">Measure</xsl:attribute>
									<xsl:value-of select="critere40"/>
								</xsl:element>
							</AnalysisResult>
						</DocumentLine>
					</xsl:if>
					<DocumentTrailer>
						<Totals TotalType="Control">
							<TotalLineCount>
								<xsl:value-of select="count(echantillon)"/>
							</TotalLineCount>
							<TotalValueBeforeTax>0.0</TotalValueBeforeTax>
							<TotalTax>0.0</TotalTax>
							<TotalValueAfterTax>0.0</TotalValueAfterTax>
						</Totals>
					</DocumentTrailer>
				</QualityDocument>
			</xsl:for-each>
			<EnvelopeTrailer>
				<TotalMessageCount>
					<xsl:value-of select="count(segment_Detail/record_)"/>
				</TotalMessageCount>
			</EnvelopeTrailer>
		</Envelope>
	</xsl:template>
	<xsl:template name="f_dt">
		<!-- YYYY-MM-DDTHH:MM:SS to YYYYMMDD HH:MM:SS-->
		<xsl:param name="val"/>
		<xsl:value-of select="substring($val, 1, 4)"/>
		<xsl:value-of select="substring($val, 6, 2)"/>
		<xsl:value-of select="substring($val, 9, 2)"/>
		<xsl:text> </xsl:text>
		<xsl:value-of select="substring($val, 12, 8)"/>
	</xsl:template>
</xsl:stylesheet><!-- Stylus Studio meta-information - (c)1998-2004. Sonic Software Corporation. All rights reserved.
<metaInformation>
<scenarios ><scenario default="yes" name="Scenario1" userelativepaths="yes" externalpreview="no" url="..\..\AdapterFramework2.2.0_F4F\messages\germstest\test_xml.xml" htmlbaseurl="" outputurl="" processortype="internal" profilemode="0" urlprofilexml="" commandline="" additionalpath="" additionalclasspath="" postprocessortype="none" postprocesscommandline="" postprocessadditionalpath="" postprocessgeneratedext=""/></scenarios><MapperInfo srcSchemaPath="" srcSchemaRoot="" srcSchemaPathIsRelative="yes" srcSchemaInterpretAsXML="no" destSchemaPath="" destSchemaRoot="" destSchemaPathIsRelative="yes" destSchemaInterpretAsXML="no"/>
</metaInformation>
-->