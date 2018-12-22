package com.ebxps.cadif.adpatation;

import com.ebxps.cadif.*;
@Deprecated
public class SourceFileBean {

	private String cadiFileName;
	private String cadiFileFolder;
	private String cadiFileImportMode;
	private CadiCsvImportAttributes cadiCsvImportAttributes;
	private CadiXmlImportAttributes cadiXmlImportAttributes;
	private String cadiArchiveFolder;
	private Integer cadiKeepArchiveHours;
	private String cadiFailedFolderPath;
	private Integer cadiKeepFailedHours;
	private String cadiBatchIdRegex;

	public String getCadiFileName() {
		return cadiFileName;
	}

	public void setCadiFileName(String cadiFileName) {
		this.cadiFileName = cadiFileName;
	}

	public String getCadiFileFolder() {
		return cadiFileFolder;
	}

	public void setCadiFileFolder(String cadiFileFolder) {
		this.cadiFileFolder = cadiFileFolder;
	}

	public String getCadiFileImportMode() {
		return cadiFileImportMode;
	}

	public void setCadiFileImportMode(String cadiFileImportMode) {
		this.cadiFileImportMode = cadiFileImportMode;
	}

	public CadiCsvImportAttributes getCadiCsvImportAttributes() {
		return cadiCsvImportAttributes;
	}

	public void setCadiCsvImportAttributes(CadiCsvImportAttributes cadiCsvImportAttributes) {
		this.cadiCsvImportAttributes = cadiCsvImportAttributes;
	}

	public CadiXmlImportAttributes getCadiXmlImportAttributes() {
		return cadiXmlImportAttributes;
	}

	public void setCadiXmlImportAttributes(CadiXmlImportAttributes cadiXmlImportAttributes) {
		this.cadiXmlImportAttributes = cadiXmlImportAttributes;
	}

	public String getCadiArchiveFolder() {
		return cadiArchiveFolder;
	}

	public void setCadiArchiveFolder(String cadiArchiveFolder) {
		this.cadiArchiveFolder = cadiArchiveFolder;
	}

	public Integer getCadiKeepArchiveHours() {
		return cadiKeepArchiveHours;
	}

	public void setCadiKeepArchiveHours(Integer cadiKeepArchiveHours) {
		this.cadiKeepArchiveHours = cadiKeepArchiveHours;
	}

	public String getCadiFailedFolderPath() {
		return cadiFailedFolderPath;
	}

	public void setCadiFailedFolderPath(String cadiFailedFolderPath) {
		this.cadiFailedFolderPath = cadiFailedFolderPath;
	}

	public Integer getCadiKeepFailedHours() {
		return cadiKeepFailedHours;
	}

	public void setCadiKeepFailedHours(Integer cadiKeepFailedHours) {
		this.cadiKeepFailedHours = cadiKeepFailedHours;
	}

	public String getCadiBatchIdRegex() {
		return cadiBatchIdRegex;
	}

	public void setCadiBatchIdRegex(String cadiBatchIdRegex) {
		this.cadiBatchIdRegex = cadiBatchIdRegex;
	}



	@Deprecated
	private class CadiCsvImportAttributes {
		private String cadiImportMode;
		private String cadiFileEncoding;
		private String cadiColumnHeader;
		private String cadiFieldSeparator;
		private String cadiFieldSeparatorChar;
		private String cadiListSeparator;
		private String cadiListSeparatorChar;
		public String getCadiImportMode() {
			return cadiImportMode;
		}
		public void setCadiImportMode(String cadiImportMode) {
			this.cadiImportMode = cadiImportMode;
		}
		public String getCadiFileEncoding() {
			return cadiFileEncoding;
		}
		public void setCadiFileEncoding(String cadiFileEncoding) {
			this.cadiFileEncoding = cadiFileEncoding;
		}
		public String getCadiColumnHeader() {
			return cadiColumnHeader;
		}
		public void setCadiColumnHeader(String cadiColumnHeader) {
			this.cadiColumnHeader = cadiColumnHeader;
		}
		public String getCadiFieldSeparator() {
			return cadiFieldSeparator;
		}
		public void setCadiFieldSeparator(String cadiFieldSeparator) {
			this.cadiFieldSeparator = cadiFieldSeparator;
		}
		public String getCadiFieldSeparatorChar() {
			return cadiFieldSeparatorChar;
		}
		public void setCadiFieldSeparatorChar(String cadiFieldSeparatorChar) {
			this.cadiFieldSeparatorChar = cadiFieldSeparatorChar;
		}
		public String getCadiListSeparator() {
			return cadiListSeparator;
		}
		public void setCadiListSeparator(String cadiListSeparator) {
			this.cadiListSeparator = cadiListSeparator;
		}
		public String getCadiListSeparatorChar() {
			return cadiListSeparatorChar;
		}
		public void setCadiListSeparatorChar(String cadiListSeparatorChar) {
			this.cadiListSeparatorChar = cadiListSeparatorChar;
		}



	}

	private class CadiXmlImportAttributes {
		private String cadiImportMode;
		private boolean cadiImportByDelta;
		private boolean cadiSetMissingtoNull;
		private boolean cadiIgnoreExtraCols;
		public String getCadiImportMode() {
			return cadiImportMode;
		}
		public void setCadiImportMode(String cadiImportMode) {
			this.cadiImportMode = cadiImportMode;
		}
		public boolean isCadiImportByDelta() {
			return cadiImportByDelta;
		}
		public void setCadiImportByDelta(boolean cadiImportByDelta) {
			this.cadiImportByDelta = cadiImportByDelta;
		}
		public boolean isCadiSetMissingtoNull() {
			return cadiSetMissingtoNull;
		}
		public void setCadiSetMissingtoNull(boolean cadiSetMissingtoNull) {
			this.cadiSetMissingtoNull = cadiSetMissingtoNull;
		}
		public boolean isCadiIgnoreExtraCols() {
			return cadiIgnoreExtraCols;
		}
		public void setCadiIgnoreExtraCols(boolean cadiIgnoreExtraCols) {
			this.cadiIgnoreExtraCols = cadiIgnoreExtraCols;
		}

	}


}

