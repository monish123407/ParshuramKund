import { jsPDF } from 'jspdf';

export interface ApplicantPassData {
  id: string | number;
  fullName: string;
  age: number | string;
  gender: string;
  phone: string;
  email: string;
  holyDipDate: string;
  bookingDate: string;
  aadharNumber: string;
  presentAddress: string;
  presentDistrict: string;
  presentState: string;
  presentPinCode: string;
  permanentAddress: string;
  permanentDistrict: string;
  permanentState: string;
  permanentPinCode: string;
  isPresentCoApplicant: boolean | string;
  coApplicant?: string; // JSON string representing co-applicants list
}

// Helper to load image
function loadImage(url: string): Promise<HTMLImageElement> {
  return new Promise((resolve, reject) => {
    const img = new Image();
    img.crossOrigin = 'Anonymous';
    img.onload = () => resolve(img);
    img.onerror = (e) => reject(e);
    img.src = url;
  });
}

export async function generateClientPdf(data: ApplicantPassData): Promise<jsPDF> {
  const doc = new jsPDF({
    orientation: 'portrait',
    unit: 'mm',
    format: 'a4'
  });

  const primaryColor = [26, 42, 64];   // #1a2a40
  const goldColor = [217, 119, 6];     // #d97706
  const labelBg = [248, 250, 252];    // #f8fafc
  const borderColor = [226, 232, 240];// #e2e8f0
  const textColor = [0, 0, 0];
  const labelTextColor = [71, 85, 105];// #475569

  // Load logos and QR codes in parallel
  const logoUrl = 'assets/Parshuramlogo.png';
  const qrDataStr = `${data.id}`;
  const qrUrl = `https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${encodeURIComponent(qrDataStr)}`;

  let logoImg: HTMLImageElement | null = null;
  let qrImg: HTMLImageElement | null = null;

  try {
    const [loadedLogo, loadedQr] = await Promise.all([
      loadImage(logoUrl).catch(() => null),
      loadImage(qrUrl).catch(() => null)
    ]);
    logoImg = loadedLogo;
    qrImg = loadedQr;
  } catch (e) {
    console.error('Error loading images for PDF generation:', e);
  }

  // Draw Header Logo
  if (logoImg) {
    doc.addImage(logoImg, 'PNG', 15, 15, 16, 16);
  }

  // Draw Title Text
  doc.setFont('helvetica', 'bold');
  doc.setFontSize(13);
  doc.setTextColor(primaryColor[0], primaryColor[1], primaryColor[2]);
  doc.text('PARSHURAM KUND MAKAR SANKRANTI MELA 2027', 36, 21);

  doc.setFont('helvetica', 'bold');
  doc.setFontSize(8.5);
  doc.setTextColor(labelTextColor[0], labelTextColor[1], labelTextColor[2]);
  doc.text('Government of Arunachal Pradesh | Pilgrim Entry Pass', 36, 27);

  // Divider Line
  doc.setFillColor(goldColor[0], goldColor[1], goldColor[2]);
  doc.rect(15, 34, 180, 0.8, 'F');

  // Status Check
  doc.setFont('helvetica', 'bold');
  doc.setFontSize(11);
  doc.setTextColor(22, 163, 74); // Green
  doc.text('REGISTRATION CONFIRMED', 105, 42, { align: 'center' });

  // Draw Primary Details Table Header
  doc.setFont('helvetica', 'bold');
  doc.setFontSize(9.5);
  doc.setTextColor(primaryColor[0], primaryColor[1], primaryColor[2]);
  doc.text('PRIMARY PILGRIM DETAILS', 15, 48);

  // Table Coordinates
  let currentY = 51;
  const startX = 15;
  const colWidth = 45; // 4 columns total: 45 * 4 = 180
  const rowHeight = 8;

  // Helper to draw cell
  const drawCell = (x: number, y: number, w: number, h: number, label: string, val: string) => {
    // Draw background for label
    doc.setFillColor(labelBg[0], labelBg[1], labelBg[2]);
    doc.rect(x, y, w * 0.45, h, 'F');
    // Draw borders
    doc.setDrawColor(borderColor[0], borderColor[1], borderColor[2]);
    doc.rect(x, y, w * 0.45, h, 'S');
    doc.rect(x + w * 0.45, y, w * 0.55, h, 'S');

    // Text for label
    doc.setFont('helvetica', 'bold');
    doc.setFontSize(8);
    doc.setTextColor(labelTextColor[0], labelTextColor[1], labelTextColor[2]);
    doc.text(label, x + 2, y + 5);

    // Text for value
    doc.setFont('helvetica', 'normal');
    doc.setFontSize(8);
    doc.setTextColor(textColor[0], textColor[1], textColor[2]);
    const splitValue = doc.splitTextToSize(val || '', w * 0.53);
    doc.text(splitValue, x + w * 0.45 + 2, y + 5);
  };

  // Helper to draw full-width row
  const drawFullRow = (x: number, y: number, w: number, h: number, label: string, val: string) => {
    doc.setFillColor(labelBg[0], labelBg[1], labelBg[2]);
    doc.rect(x, y, w * 0.15, h, 'F');
    doc.setDrawColor(borderColor[0], borderColor[1], borderColor[2]);
    doc.rect(x, y, w * 0.15, h, 'S');
    doc.rect(x + w * 0.15, y, w * 0.85, h, 'S');

    doc.setFont('helvetica', 'bold');
    doc.setFontSize(8);
    doc.setTextColor(labelTextColor[0], labelTextColor[1], labelTextColor[2]);
    doc.text(label, x + 2, y + 5);

    doc.setFont('helvetica', 'normal');
    doc.setFontSize(8);
    doc.setTextColor(textColor[0], textColor[1], textColor[2]);
    const splitValue = doc.splitTextToSize(val || '', w * 0.83);
    doc.text(splitValue, x + w * 0.15 + 2, y + 5);
  };

  // Row 1
  drawCell(startX, currentY, colWidth * 2, rowHeight, 'Registration ID:', String(data.id));
  drawCell(startX + colWidth * 2, currentY, colWidth * 2, rowHeight, 'Booking Date:', data.bookingDate);
  currentY += rowHeight;

  // Row 2
  drawCell(startX, currentY, colWidth * 2, rowHeight, 'Pilgrim Name:', data.fullName);
  drawCell(startX + colWidth * 2, currentY, colWidth * 2, rowHeight, 'Age / Gender:', `${data.age} / ${data.gender}`);
  currentY += rowHeight;

  // Row 3
  drawCell(startX, currentY, colWidth * 2, rowHeight, 'Holy Dip Date:', data.holyDipDate);
  drawCell(startX + colWidth * 2, currentY, colWidth * 2, rowHeight, 'Contact Phone:', data.phone || 'Not Provided');
  currentY += rowHeight;

  // Row 4
  drawCell(startX, currentY, colWidth * 2, rowHeight, 'Aadhaar Number:', data.aadharNumber || 'Not Provided');
  drawCell(startX + colWidth * 2, currentY, colWidth * 2, rowHeight, 'Email Address:', data.email || 'Not Provided');
  currentY += rowHeight;

  // Present Address
  const presentAddr = `${data.presentAddress}, ${data.presentDistrict}, ${data.presentState} - ${data.presentPinCode}`;
  drawFullRow(startX, currentY, colWidth * 4, rowHeight * 1.2, 'Present Address:', presentAddr);
  currentY += rowHeight * 1.2;

  // Permanent Address
  const permAddr = `${data.permanentAddress}, ${data.permanentDistrict}, ${data.permanentState} - ${data.permanentPinCode}`;
  drawFullRow(startX, currentY, colWidth * 4, rowHeight * 1.2, 'Permanent Address:', permAddr);
  currentY += rowHeight * 1.2;

  // Draw Co-Applicants Table if present
  let coApplicants: any[] = [];
  const isCoApp = data.isPresentCoApplicant === true || data.isPresentCoApplicant === 'Yes';
  if (isCoApp && data.coApplicant) {
    try {
      const parsed = JSON.parse(data.coApplicant);
      if (Array.isArray(parsed) && parsed.length > 0) {
        coApplicants = parsed;
      }
    } catch (e) {
      console.error('Error parsing coApplicants for PDF:', e);
    }
  }

  if (coApplicants.length > 0) {
    currentY += 5;
    doc.setFont('helvetica', 'bold');
    doc.setFontSize(9.5);
    doc.setTextColor(primaryColor[0], primaryColor[1], primaryColor[2]);
    doc.text('CO-TRAVELERS DETAILS', 15, currentY);
    currentY += 3;

    // Draw table headers
    const coHeaders = ['Name', 'Age', 'Gender', 'Co-morbidities'];
    const coWidths = [70, 25, 35, 50]; // Total = 180
    
    // Draw header backgrounds
    doc.setFillColor(primaryColor[0], primaryColor[1], primaryColor[2]);
    doc.rect(startX, currentY, 180, 7, 'F');

    doc.setFont('helvetica', 'bold');
    doc.setFontSize(8);
    doc.setTextColor(255, 255, 255);
    
    let currentX = startX;
    for (let i = 0; i < coHeaders.length; i++) {
      doc.text(coHeaders[i], currentX + 2, currentY + 4.8);
      currentX += coWidths[i];
    }
    currentY += 7;

    // Draw rows
    doc.setFont('helvetica', 'normal');
    doc.setTextColor(textColor[0], textColor[1], textColor[2]);

    for (const p of coApplicants) {
      // Draw background border
      doc.setDrawColor(borderColor[0], borderColor[1], borderColor[2]);
      doc.rect(startX, currentY, 180, 6.5, 'S');

      let rowX = startX;
      
      // Name
      doc.text(p.name || '', rowX + 2, currentY + 4.5);
      doc.line(rowX + coWidths[0], currentY, rowX + coWidths[0], currentY + 6.5);
      rowX += coWidths[0];

      // Age
      doc.text(String(p.age || ''), rowX + 2, currentY + 4.5);
      doc.line(rowX + coWidths[1], currentY, rowX + coWidths[1], currentY + 6.5);
      rowX += coWidths[1];

      // Gender
      doc.text(p.gender || '', rowX + 2, currentY + 4.5);
      doc.line(rowX + coWidths[2], currentY, rowX + coWidths[2], currentY + 6.5);
      rowX += coWidths[2];

      // Co-morbidities
      const cmText = p.comorbidities || 'None';
      doc.text(cmText, rowX + 2, currentY + 4.5);
      
      currentY += 6.5;
    }
  }

  // Draw Instructions and QR Code section
  currentY += 6;
  
  // Left side: Instructions
  doc.setFont('helvetica', 'bold');
  doc.setFontSize(9);
  doc.setTextColor(primaryColor[0], primaryColor[1], primaryColor[2]);
  doc.text('IMPORTANT INSTRUCTIONS:', 15, currentY);
  currentY += 4.5;

  const instructions = [
    '1. Please carry a printed copy of this pass and a valid original photo identity proof (Aadhaar Card, Voter ID, etc.) for verification at entry checkpoints.',
    '2. Present this pass (either physical or digital copy on mobile) to the scanning officials upon arrival.',
    '3. Adhere strictly to the security, health guidelines, and sanitization protocols established by the Mela administration.',
    '4. Holy dip is permitted only at the designated bathing ghats under the surveillance of lifeguards and disaster management teams.',
    '5. In case of medical or security assistance, contact the nearest transit camp or call the emergency response cell.'
  ];

  doc.setFont('helvetica', 'normal');
  doc.setFontSize(7.2);
  doc.setTextColor(labelTextColor[0], labelTextColor[1], labelTextColor[2]);
  
  let instructionsY = currentY;
  for (const inst of instructions) {
    const splitInst = doc.splitTextToSize(inst, 120);
    doc.text(splitInst, 15, instructionsY);
    instructionsY += splitInst.length * 3.3;
  }

  // Right side: QR Code
  if (qrImg) {
    doc.setFillColor(250, 250, 250);
    doc.setDrawColor(borderColor[0], borderColor[1], borderColor[2]);
    doc.rect(143, currentY - 5, 54, 62, 'FD');

    doc.addImage(qrImg, 'PNG', 150, currentY - 2, 40, 40);

    doc.setFont('helvetica', 'bold');
    doc.setFontSize(7.5);
    doc.setTextColor(primaryColor[0], primaryColor[1], primaryColor[2]);
    doc.text('Scan to Verify', 170, currentY + 42.5, { align: 'center' });

    const todayStr = data.bookingDate || '';
    doc.setFont('helvetica', 'normal');
    doc.setFontSize(6.5);
    doc.setTextColor(120, 120, 120);
    doc.text(`Issued: ${todayStr}`, 170, currentY + 47, { align: 'center' });
  }

  currentY = Math.max(instructionsY, currentY + 62);

  // Disclaimer text
  doc.setFont('helvetica', 'italic');
  doc.setFontSize(7.5);
  doc.setTextColor(120, 120, 120);
  doc.text('This is a system-generated Pilgrim Entry Pass. No physical signature is required.', 105, currentY + 8, { align: 'center' });

  return doc;
}
