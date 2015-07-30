<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns:java="http://xml.apache.org/xslt/java" exclude-result-prefixes="java">
  <xsl:output method="text"/>
  
  <xsl:variable name="SOURCE" select="/F4FInvoice/InvoiceHeader/TradingPartner[@PartnerType='Supplier']/PartnerID[@PartnerIDType='Assigned by F4F']"/>
  <xsl:variable name="DEST" select="/F4FInvoice/InvoiceHeader/TradingPartner[@PartnerType='Buyer']/PartnerID[@PartnerIDType='Assigned by F4F']"/>

  <xsl:template match="/F4FInvoice">
    <xsl:for-each select="*">
      <xsl:choose>
        <xsl:when test="name()='F4FDocumentHeader'"><xsl:call-template name="f4fheader"/></xsl:when>
        <xsl:when test="name()='InvoiceHeader'"><xsl:call-template name="invoiceheader"/></xsl:when>
        <xsl:when test="name()='TransportDetails'">
          <xsl:call-template name="transportdetails">
            <xsl:with-param name="segid" select="'005'"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:when test="name()='InvoiceLine'"><xsl:call-template name="invoiceline"/></xsl:when>
        <xsl:when test="name()='AdditionalValues'"><xsl:call-template name="additionalvalues"/></xsl:when>
        <xsl:when test="name()='TaxTrailer'"><xsl:call-template name="taxtrailer"/></xsl:when>
        <xsl:when test="name()='InvoiceControlTotals'"><xsl:call-template name="controltotals"/></xsl:when>
      </xsl:choose>
    </xsl:for-each>
  </xsl:template>
  
  <xsl:template name="additionalvalues">
    <xsl:text>020&#10;</xsl:text>
    <xsl:for-each select="AdditionalTax">
      <xsl:text>ADT,</xsl:text>
      <xsl:value-of select="."/><xsl:text>,</xsl:text>
      <xsl:value-of select="@Percentage"/><xsl:text>,</xsl:text>
      <xsl:value-of select="@Code"/><xsl:text>,</xsl:text>
      <xsl:value-of select="@Description"/>
      <xsl:text>&#10;</xsl:text>
    </xsl:for-each>

    <xsl:for-each select="AdditionalPremium">
      <xsl:text>ADP,</xsl:text>
      <xsl:value-of select="."/><xsl:text>,</xsl:text>
      <xsl:value-of select="@TaxCode"/><xsl:text>,</xsl:text>
      <xsl:value-of select="@TaxPercentage"/><xsl:text>,</xsl:text>
      <xsl:value-of select="@TaxValue"/><xsl:text>,</xsl:text>
      <xsl:value-of select="@NetValue"/><xsl:text>,</xsl:text>
      <xsl:value-of select="@SundryType"/><xsl:text>,</xsl:text>
      <xsl:value-of select="@Rate"/><xsl:text>,</xsl:text>
      <xsl:value-of select="@Basis"/>
      <xsl:text>&#10;</xsl:text>
    </xsl:for-each>

    <xsl:for-each select="AdditionalDiscount">
      <xsl:text>ADI,</xsl:text>
      <xsl:value-of select="."/><xsl:text>,</xsl:text>
      <xsl:value-of select="@Percentage"/><xsl:text>,</xsl:text>
      <xsl:value-of select="@Description"/>
      <xsl:text>&#10;</xsl:text>
    </xsl:for-each>

    <xsl:for-each select="AdditionalDeduction">
      <xsl:text>ADE,</xsl:text>
      <xsl:value-of select="."/><xsl:text>,</xsl:text>
      <xsl:value-of select="@TaxCode"/><xsl:text>,</xsl:text>
      <xsl:value-of select="@TaxPercentage"/><xsl:text>,</xsl:text>
      <xsl:value-of select="@TaxValue"/><xsl:text>,</xsl:text>
      <xsl:value-of select="@NetValue"/><xsl:text>,</xsl:text>
      <xsl:value-of select="@SundryType"/><xsl:text>,</xsl:text>
      <xsl:value-of select="@Rate"/><xsl:text>,</xsl:text>
      <xsl:value-of select="@Basis"/>
      <xsl:text>&#10;</xsl:text>
    </xsl:for-each>

    <xsl:for-each select="AdditionalQualityAdjustment">
      <xsl:text>AQA,</xsl:text>
      <xsl:call-template name="qualityadjustment"/>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="address">
    <xsl:param name="segid"/>
    <xsl:value-of select="$segid+1"/>
    <xsl:text>&#10;</xsl:text>
    
    <xsl:for-each select="AddressID">
      <xsl:text>AID,</xsl:text>
      <xsl:value-of select="."/><xsl:text>,</xsl:text>
      <xsl:value-of select="@AddressIDType"/>
      <xsl:text>&#10;</xsl:text>
    </xsl:for-each>
    
    <xsl:text>ADR,</xsl:text>
    <xsl:value-of select="@AddressType"/><xsl:text>,</xsl:text>
    <xsl:value-of select="CompanyName"/><xsl:text>,</xsl:text>
    <xsl:value-of select="StreetAddress[1]"/><xsl:text>,</xsl:text>
    <xsl:value-of select="StreetAddress[2]"/><xsl:text>,</xsl:text>
    <xsl:value-of select="StreetAddress[3]"/><xsl:text>,</xsl:text>
    <xsl:value-of select="StreetAddress[4]"/><xsl:text>,</xsl:text>
    <xsl:value-of select="StreetAddress[5]"/><xsl:text>,</xsl:text>
    <xsl:value-of select="CityName"/><xsl:text>,</xsl:text>
    <xsl:value-of select="CountyDistrictName"/><xsl:text>,</xsl:text>
    <xsl:value-of select="PostalCode"/><xsl:text>,</xsl:text>
    <xsl:value-of select="CountryCode"/><xsl:text>,</xsl:text>
    <xsl:value-of select="LocationID"/><xsl:text>,</xsl:text>
    <xsl:value-of select="LocationID/@LocationIDType"/>
    <xsl:text>&#10;</xsl:text>

    <xsl:for-each select="AddressContact">
      <xsl:text>ACR,</xsl:text>
      <xsl:call-template name="contact"/>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="contact">
    <xsl:value-of select="ContactName"/><xsl:text>,</xsl:text>
    <xsl:value-of select="ContactInfo[@Method='Primary Telephone']"/><xsl:text>,</xsl:text>
    <xsl:value-of select="ContactInfo[@Method='Secondary Telephone']"/><xsl:text>,</xsl:text>
    <xsl:value-of select="ContactInfo[@Method='Voicemail']"/><xsl:text>,</xsl:text>
    <xsl:value-of select="ContactInfo[@Method='Facsimile']"/><xsl:text>,</xsl:text>
    <xsl:value-of select="ContactInfo[@Method='Telex']"/><xsl:text>,</xsl:text>
    <xsl:value-of select="ContactInfo[@Method='Mobile Telephone']"/><xsl:text>,</xsl:text>
    <xsl:value-of select="ContactInfo[@Method='Email Address']"/>
    <xsl:text>&#10;</xsl:text>
  </xsl:template>

  <xsl:template name="controltotals">
    <xsl:text>021&#10;</xsl:text> 
    <xsl:text>DCT,</xsl:text>
    <xsl:value-of select="TotalNumberOfLines"/><xsl:text>,</xsl:text>
    <xsl:value-of select="TotalQuantity"/><xsl:text>,</xsl:text>
    <xsl:value-of select="TotalNetValue"/><xsl:text>,</xsl:text>
    <xsl:value-of select="TotalLineDiscount"/><xsl:text>,</xsl:text>
    <xsl:value-of select="TotalLineTax"/><xsl:text>,</xsl:text>
    <xsl:value-of select="TotalLineQualityAdjustment"/><xsl:text>,</xsl:text>
    <xsl:value-of select="TotalAdditionalAdjustments"/><xsl:text>,</xsl:text>
    <xsl:value-of select="TotalGrossValue"/>
    <xsl:text>&#10;</xsl:text>

    <xsl:for-each select="Narrative">
      <xsl:call-template name="narrative"/>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="date">
    <xsl:value-of select="."/><xsl:text>,</xsl:text>
    <xsl:value-of select="@DateTimeType"/>
    <xsl:text>&#10;</xsl:text>
  </xsl:template>

  <xsl:template name="f4fheader">
    <xsl:text>DHR,</xsl:text>
    <xsl:value-of select="../@InvoiceType"/><xsl:text>,</xsl:text>
    <xsl:value-of select="../@DocumentType"/><xsl:text>,</xsl:text>
    <xsl:value-of select="../@TypeOfSupply"/><xsl:text>,</xsl:text>
    <xsl:value-of select="SchemaVersion"/><xsl:text>,</xsl:text>
    <xsl:value-of select="SchemaStatus"/><xsl:text>,</xsl:text>
    <xsl:value-of select="DocumentCreated"/><xsl:text>,</xsl:text>
    <xsl:value-of select="DocumentCreated/@DateTimeType"/><xsl:text>,</xsl:text>
    <xsl:value-of select="DocumentTrackingId"/><xsl:text>,</xsl:text>
    <xsl:value-of select="DocumentRevisionNumber"/><xsl:text>,</xsl:text>
    <xsl:value-of select="SourcePartnerID"/><xsl:text>,</xsl:text>
    <xsl:value-of select="SourceDivisionID"/><xsl:text>,</xsl:text>
    <xsl:value-of select="DestinationPartnerID"/><xsl:text>,</xsl:text>
    <xsl:value-of select="DestinationDivisionID"/>
    <xsl:text>&#10;</xsl:text>
  </xsl:template>

  <xsl:template name="invoiceheader">
    <xsl:text>001&#10;</xsl:text>
    <xsl:for-each select="InvoiceReference">
      <xsl:text>DRR,</xsl:text>
      <xsl:call-template name="reference"/>
    </xsl:for-each>

    <xsl:for-each select="InvoiceDate">
      <xsl:text>DDT,</xsl:text>
      <xsl:call-template name="date"/>
    </xsl:for-each>

    <xsl:for-each select="InvoiceContact">
      <xsl:text>DCR,</xsl:text>
      <xsl:call-template name="contact"/>
    </xsl:for-each>

    <xsl:for-each select="TradingPartner">
      <xsl:call-template name="tradingpartner">
        <xsl:with-param name="segid" select="'001'"/>
      </xsl:call-template>
    </xsl:for-each>

    <xsl:for-each select="TermsAndConditions">
      <xsl:text>005&#10;</xsl:text>
      <xsl:call-template name="termsandconditions"/>
    </xsl:for-each>

    <xsl:for-each select="InvoiceReason">
      <xsl:text>REA,</xsl:text>
      <xsl:value-of select="."/>
      <xsl:text>&#10;</xsl:text> 
    </xsl:for-each>

    <xsl:for-each select="Narrative">
      <xsl:call-template name="narrative"/>
    </xsl:for-each>
  </xsl:template>
  
  <xsl:template name="invoiceline">
    <xsl:text>012&#10;</xsl:text> 
    <xsl:text>ILN,</xsl:text>
    <xsl:value-of select="@Status"/><xsl:text>,</xsl:text>
    <xsl:value-of select="@LineNumber"/><xsl:text>,</xsl:text>
    <xsl:value-of select="ProductValues/@Currency"/><xsl:text>,</xsl:text>
    <xsl:value-of select="ProductValues/UnitPrice"/><xsl:text>,</xsl:text>
    <xsl:value-of select="ProductValues/UnitPrice/@TaxCode"/><xsl:text>,</xsl:text>
    <xsl:value-of select="ProductValues/UnitPrice/@TaxPercentage"/><xsl:text>,</xsl:text>
    <xsl:value-of select="ProductValues/UnitPrice/@TaxValue"/><xsl:text>,</xsl:text>
    <xsl:value-of select="ProductValues/UnitPrice/@NetValue"/><xsl:text>,</xsl:text>
    <xsl:value-of select="ProductValues/UnitPrice/@PriceUOM"/><xsl:text>,</xsl:text>
    <xsl:value-of select="ProductValues/ListPrice"/><xsl:text>,</xsl:text>
    <xsl:value-of select="ProductValues/ListPrice/@TaxCode"/><xsl:text>,</xsl:text>
    <xsl:value-of select="ProductValues/ListPrice/@TaxPercentage"/><xsl:text>,</xsl:text>
    <xsl:value-of select="ProductValues/ListPrice/@TaxValue"/><xsl:text>,</xsl:text>
    <xsl:value-of select="ProductValues/ListPrice/@NetValue"/><xsl:text>,</xsl:text>
    <xsl:value-of select="ProductValues/ListPrice/@PriceUOM"/><xsl:text>,</xsl:text>
    <xsl:value-of select="ProductValues/GoodsNet"/><xsl:text>,</xsl:text>
    <xsl:value-of select="ProductValues/GoodsGross"/><xsl:text>,</xsl:text>
    <xsl:value-of select="ProductValues/GoodsGross/@TaxCode"/><xsl:text>,</xsl:text>
    <xsl:value-of select="ProductValues/GoodsGross/@TaxPercentage"/><xsl:text>,</xsl:text>
    <xsl:value-of select="ProductValues/GoodsGross/@TaxValue"/><xsl:text>,</xsl:text>
    <xsl:value-of select="ProductValues/GoodsGross/@NetValue"/><xsl:text>,</xsl:text>
    <xsl:value-of select="ProductValues/LineNetValue"/><xsl:text>,</xsl:text>
    <xsl:value-of select="ProductValues/LineGrossValue"/><xsl:text>,</xsl:text>
    <xsl:text>&#10;</xsl:text>

    <xsl:for-each select="ProductValues/Quantity">
      <xsl:text>QTY,</xsl:text>
      <xsl:value-of select="."/><xsl:text>,</xsl:text>
      <xsl:value-of select="@QuantityType"/><xsl:text>,</xsl:text>
      <xsl:value-of select="@QuantityUOM"/>
      <xsl:text>&#10;</xsl:text>
    </xsl:for-each>
    
    <xsl:for-each select="ProductWeights">
      <xsl:text>PRW,</xsl:text>
      <xsl:value-of select="WeightDetails/@WeighedBy"/><xsl:text>,</xsl:text>
      <xsl:value-of select="WeightDetails[@Type='Net']/Weight"/><xsl:text>,</xsl:text>
      <xsl:value-of select="WeightDetails[@Type='Net']/Weight/@WeightUOM"/><xsl:text>,</xsl:text>
      <xsl:value-of select="WeightDetails[@Type='Tare']/Weight"/><xsl:text>,</xsl:text>
      <xsl:value-of select="WeightDetails[@Type='Tare']/Weight/@WeightUOM"/><xsl:text>,</xsl:text>    
      <xsl:value-of select="WeightDetails[@Type='Gross']/Weight"/><xsl:text>,</xsl:text>
      <xsl:value-of select="WeightDetails[@Type='Gross']/Weight/@WeightUOM"/><xsl:text>,</xsl:text>
      <xsl:value-of select="WeightDetails/WeighedDate"/><xsl:text>,</xsl:text>
      <xsl:value-of select="WeightDetails/WeighedDate/@DateTimeType"/><xsl:text>,</xsl:text>
      <xsl:value-of select="WeightDetails/WeighbridgeReference"/><xsl:text>,</xsl:text>
      <xsl:value-of select="WeightDetails/WeighbridgeReference/@AssignedBy"/><xsl:text>,</xsl:text>
      <xsl:value-of select="WeightDetails/WeighbridgeReference/@ReferenceType"/>            
      <xsl:text>&#10;</xsl:text>
    </xsl:for-each>


    <xsl:for-each select="ProductValues/QualityDeduction">
      <xsl:text>AQD,</xsl:text>
      <xsl:call-template name = "qualityadjustment"/>
    </xsl:for-each>
    
    <xsl:for-each select="ProductValues/QualityPremium">
       <xsl:text>AQP,</xsl:text>
       <xsl:call-template name = "qualityadjustment"/>
    </xsl:for-each>

    <xsl:for-each select="TransportDetails">
      <xsl:call-template name="transportdetails">
        <xsl:with-param name="segid" select="'012'"/>
      </xsl:call-template>
    </xsl:for-each>

    <xsl:for-each select="ProductReference">
      <xsl:if test="position() = 1">
        <xsl:text>019&#10;</xsl:text> 
      </xsl:if>
      <xsl:text>PRR,</xsl:text>
      <xsl:value-of select="."/><xsl:text>,</xsl:text>
      <xsl:value-of select="@ReferenceType"/>
      <xsl:text>&#10;</xsl:text>
    </xsl:for-each>

    <xsl:for-each select="ProductDescription">
      <xsl:text>PDR,</xsl:text>
      <xsl:value-of select="@Type"/><xsl:text>,</xsl:text>
      <xsl:value-of select="@SubType"/><xsl:text>,</xsl:text>
      <xsl:value-of select="@Sequence"/><xsl:text>,</xsl:text>
      <xsl:value-of select="."/>
      <xsl:text>&#10;</xsl:text>
    </xsl:for-each>

    <xsl:for-each select="LineReference">
      <xsl:text>LNR,</xsl:text>
      <xsl:value-of select="@LineNumber"/><xsl:text>,</xsl:text>
      <xsl:call-template name="reference"/>
    </xsl:for-each>
    
    <xsl:for-each select="ProductValues/AdditionalPremium">
      <xsl:text>LAP,</xsl:text>
      <xsl:value-of select="."/><xsl:text>,</xsl:text>
      <xsl:value-of select="@TaxCode"/><xsl:text>,</xsl:text>
      <xsl:value-of select="@TaxPercentage"/><xsl:text>,</xsl:text>
      <xsl:value-of select="@TaxValue"/><xsl:text>,</xsl:text>
      <xsl:value-of select="@NetValue"/><xsl:text>,</xsl:text>
      <xsl:value-of select="@SundryType"/><xsl:text>,</xsl:text>
      <xsl:value-of select="@Rate"/><xsl:text>,</xsl:text>
      <xsl:value-of select="@Basis"/>
      <xsl:text>&#10;</xsl:text>
    </xsl:for-each>

    <xsl:for-each select="ProductValues/AdditionalDeduction">
      <xsl:text>LAD,</xsl:text>
      <xsl:value-of select="."/><xsl:text>,</xsl:text>
      <xsl:value-of select="@TaxCode"/><xsl:text>,</xsl:text>
      <xsl:value-of select="@TaxPercentage"/><xsl:text>,</xsl:text>
      <xsl:value-of select="@TaxValue"/><xsl:text>,</xsl:text>
      <xsl:value-of select="@NetValue"/><xsl:text>,</xsl:text>
      <xsl:value-of select="@SundryType"/><xsl:text>,</xsl:text>
      <xsl:value-of select="@Rate"/><xsl:text>,</xsl:text>
      <xsl:value-of select="@Basis"/>
      <xsl:text>&#10;</xsl:text>
    </xsl:for-each>

    <xsl:for-each select="ProductValues/LineTax">
      <xsl:text>LTR,</xsl:text>
      <xsl:value-of select="."/><xsl:text>,</xsl:text>
      <xsl:value-of select="@Code"/><xsl:text>,</xsl:text>
      <xsl:value-of select="@Percentage"/><xsl:text>,</xsl:text>
      <xsl:value-of select="@Description"/>
      <xsl:text>&#10;</xsl:text>
    </xsl:for-each>

    <xsl:for-each select="Narrative">
      <xsl:call-template name="narrative"/>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="narrative">
    <xsl:text>DNA,</xsl:text>
    <xsl:value-of select="@Type"/><xsl:text>,</xsl:text>
    <xsl:value-of select="@Sequence"/><xsl:text>,</xsl:text>
    <xsl:value-of select="."/>
    <xsl:text>&#10;</xsl:text>
  </xsl:template>

  <xsl:template name="qualityadjustment">
    <xsl:value-of select="@Quality"/><xsl:text>,</xsl:text>
    <xsl:value-of select="Analysis"/><xsl:text>,</xsl:text>
    <xsl:value-of select="Analysis/@Description"/><xsl:text>,</xsl:text>
    <xsl:value-of select="Analysis/@Type"/><xsl:text>,</xsl:text>
    <xsl:value-of select="PercentageAdjustment"/><xsl:text>,</xsl:text>
    <xsl:value-of select="PercentageAdjustment/@Rate"/><xsl:text>,</xsl:text>
    <xsl:value-of select="RatioAdjustment"/><xsl:text>,</xsl:text>
    <xsl:value-of select="RatioAdjustment/@UOM"/><xsl:text>,</xsl:text>
    <xsl:value-of select="RatioAdjustment/@Type"/><xsl:text>,</xsl:text>
    <xsl:value-of select="RatioAdjustment/@Rate"/><xsl:text>,</xsl:text>
    <xsl:value-of select="Reference"/><xsl:text>,</xsl:text>
    <xsl:value-of select="Reference/@AssignedBy"/><xsl:text>,</xsl:text>
    <xsl:value-of select="Reference/@ReferenceType"/><xsl:text>,</xsl:text>
    <xsl:value-of select="AppliedValue"/><xsl:text>,</xsl:text>
    <xsl:value-of select="AppliedValue/@TaxCode"/><xsl:text>,</xsl:text>
    <xsl:value-of select="AppliedValue/@TaxPercentage"/><xsl:text>,</xsl:text>
    <xsl:value-of select="AppliedValue/@TaxValue"/><xsl:text>,</xsl:text>
    <xsl:value-of select="AppliedValue/@NetValue"/><xsl:text>,</xsl:text>
    <xsl:for-each select="Narrative">
      <xsl:call-template name="narrative"/>
    </xsl:for-each>
    <xsl:text>&#10;</xsl:text>
  </xsl:template>

  <xsl:template name="reference">
    <xsl:value-of select="."/><xsl:text>,</xsl:text>
    <xsl:value-of select="@AssignedBy"/><xsl:text>,</xsl:text>
    <xsl:value-of select="@ReferenceType"/>
    <xsl:text>&#10;</xsl:text>
  </xsl:template>

  <xsl:template name="shippinginstructions">
    <xsl:param name="segid"/>
    <xsl:value-of select="$segid+1"/>
    <xsl:text>&#10;</xsl:text> 
    
    <xsl:text>SIR,</xsl:text>
    <xsl:value-of select="@ShippingType"/><xsl:text>,</xsl:text>
    <xsl:value-of select="VehicleType"/><xsl:text>,</xsl:text>
    <xsl:text>&#10;</xsl:text>

    <xsl:for-each select="ShippingDate">
      <xsl:text>SID,</xsl:text>
      <xsl:value-of select="."/><xsl:text>,</xsl:text>
      <xsl:value-of select="@DateTimeType"/>
      <xsl:text>&#10;</xsl:text>
    </xsl:for-each>

    <xsl:for-each select="Description">
      <xsl:text>SIL,</xsl:text>
      <xsl:value-of select="@Type"/><xsl:text>,</xsl:text>
      <xsl:value-of select="@Sequence"/><xsl:text>,</xsl:text>
      <xsl:value-of select="."/>
      <xsl:text>&#10;</xsl:text>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="specialhandling">
    <xsl:text>SPH,</xsl:text>
    <xsl:value-of select="ProperShippingName"/><xsl:text>,</xsl:text>
    <xsl:value-of select="IdentificationNumber"/><xsl:text>,</xsl:text>
    <xsl:value-of select="IdentificationNumber/@Agency"/><xsl:text>,</xsl:text>
    <xsl:value-of select="IdentificationNumber/@IDType"/><xsl:text>,</xsl:text>
    <xsl:text>&#10;</xsl:text>

    <xsl:for-each select="Description">
      <xsl:text>SHD,</xsl:text>
      <xsl:value-of select="@Type"/><xsl:text>,</xsl:text>
      <xsl:value-of select="@SubType"/><xsl:text>,</xsl:text>
      <xsl:value-of select="@Sequence"/><xsl:text>,</xsl:text>
      <xsl:value-of select="."/>
      <xsl:text>&#10;</xsl:text>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="termsandconditions">
    <xsl:text>TAC,</xsl:text>
    <xsl:value-of select="@PaymentTerms"/><xsl:text>,</xsl:text>
    <xsl:value-of select="@PaymentMethod"/><xsl:text>,</xsl:text>
    <xsl:value-of select="IncotermsCode"/><xsl:text>,</xsl:text>
    <xsl:value-of select="IncotermsText"/><xsl:text>,</xsl:text>
    <xsl:value-of select="DuePayment"/><xsl:text>,</xsl:text>
    <xsl:value-of select="DuePayment/@TaxCode"/><xsl:text>,</xsl:text>
    <xsl:value-of select="DuePayment/@TaxPercentage"/><xsl:text>,</xsl:text>
    <xsl:value-of select="DuePayment/@TaxValue"/><xsl:text>,</xsl:text>
    <xsl:value-of select="DuePayment/@NetValue"/><xsl:text>,</xsl:text>
    <xsl:text>&#10;</xsl:text>

    <xsl:for-each select="DateTime">
      <xsl:text>TCD,</xsl:text>
      <xsl:call-template name="date"/>
    </xsl:for-each>

    <xsl:for-each select="Narrative">
      <xsl:text>TCN,</xsl:text>
      <xsl:value-of select="@Type"/><xsl:text>,</xsl:text>
      <xsl:value-of select="@Sequence"/><xsl:text>,</xsl:text>
      <xsl:value-of select="."/>
      <xsl:text>&#10;</xsl:text>
    </xsl:for-each>
  </xsl:template>
    
   <xsl:template name="taxtrailer">
     <xsl:text>TTR,</xsl:text>
     <xsl:value-of select="@Currency"/><xsl:text>,</xsl:text>
     <xsl:value-of select="@TaxCode"/><xsl:text>,</xsl:text>
     <xsl:value-of select="@TaxPercentage"/><xsl:text>,</xsl:text>
     <xsl:value-of select="NumberOfLines"/><xsl:text>,</xsl:text>
     <xsl:value-of select="NetValue"/><xsl:text>,</xsl:text>
     <xsl:value-of select="TaxAmount"/><xsl:text>,</xsl:text>
     <xsl:value-of select="GrossValue"/>
     <xsl:text>&#10;</xsl:text>
  </xsl:template>

  <xsl:template name="tradingpartner">
    <xsl:param name="segid"/>
    <xsl:value-of select="$segid+1"/>
    <xsl:text>&#10;</xsl:text>
    
    <xsl:text>TPR,</xsl:text>
    <xsl:value-of select="@PartnerType"/><xsl:text>,</xsl:text>
    <xsl:value-of select="CompanyName"/>
    <xsl:text>&#10;</xsl:text>

    <xsl:for-each select="PartnerID">
      <xsl:text>TPI,</xsl:text>
      <xsl:value-of select="."/><xsl:text>,</xsl:text>
      <xsl:value-of select="@PartnerIDType"/>
      <xsl:text>&#10;</xsl:text>
    </xsl:for-each>

    <xsl:for-each select="Contact">
      <xsl:text>TPC,</xsl:text>
      <xsl:call-template name="contact"/>
    </xsl:for-each>

    <xsl:for-each select="Address">
      <xsl:call-template name="address">
        <xsl:with-param name="segid" select="$segid+1"/>
      </xsl:call-template>
    </xsl:for-each>
    
    <xsl:for-each select="BankDetails">
      <xsl:text>TPB,</xsl:text>
      <xsl:value-of select="TransferRoutingCode"/><xsl:text>,</xsl:text>
      <xsl:value-of select="AccountNumber"/><xsl:text>,</xsl:text>
      <xsl:value-of select="AccountName"/>
      <xsl:text>&#10;</xsl:text>
          
      <xsl:for-each select="BankAddress">
        <xsl:call-template name="address">
          <xsl:with-param name="segid" select="$segid+2"/>
        </xsl:call-template>
      </xsl:for-each>
    </xsl:for-each>
   
  </xsl:template>
  
  <xsl:template name="transportdetails">
    <xsl:param name="segid"/>
    <xsl:value-of select="$segid+1"/>
    <xsl:text>&#10;</xsl:text>    
  
    <xsl:for-each select="TransportReference">
      <xsl:text>TRR,</xsl:text>
      <xsl:value-of select="@ReferenceType"/><xsl:text>,</xsl:text>
      <xsl:value-of select="."/>
    <xsl:text>&#10;</xsl:text>
    </xsl:for-each>

    <xsl:for-each select="TransportProvider">
      <xsl:call-template name="tradingpartner">
        <xsl:with-param name="segid" select="$segid+1"/>
      </xsl:call-template>
    </xsl:for-each>

    <xsl:for-each select="Address">
      <xsl:call-template name="address">
        <xsl:with-param name="segid" select="$segid+4"/>
      </xsl:call-template>
    </xsl:for-each>

    <xsl:for-each select="ShippingInstructions">
      <xsl:call-template name="shippinginstructions">
        <xsl:with-param name="segid" select="$segid+5"/>
      </xsl:call-template>
    </xsl:for-each>

    <xsl:for-each select="SpecialHandling">
      <xsl:call-template name="specialhandling"/>
    </xsl:for-each>
    
    <xsl:for-each select="PreviousLoad">
      <xsl:text>PRL,</xsl:text>
        <xsl:value-of select="@LoadSequence"/><xsl:text>,</xsl:text>
        <xsl:value-of select="ProductReference"/><xsl:text>,</xsl:text>
        <xsl:value-of select="ProductReference/@ReferenceType"/><xsl:text>,</xsl:text>
        <xsl:value-of select="ProductDescription/@Type"/><xsl:text>,</xsl:text>
        <xsl:value-of select="ProductDescription/@SubType"/><xsl:text>,</xsl:text>
        <xsl:value-of select="ProductDescription/@Sequence"/><xsl:text>,</xsl:text>
        <xsl:value-of select="ProductDescription"/>
      </xsl:for-each>
    <xsl:text>&#10;</xsl:text>
    
  </xsl:template>
</xsl:stylesheet>
