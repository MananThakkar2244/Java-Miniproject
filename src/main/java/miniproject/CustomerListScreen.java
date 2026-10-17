package miniproject;

import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.jline.terminal.Terminal;

import miniproject.dao.AccountRepository;
import miniproject.exception.AccountNotFoundException;
import miniproject.exception.DataAccessException;
import miniproject.model.AccountRecord;
import miniproject.model.Customer;
import miniproject.model.LoanAccount;

class CustomerListScreen implements VariablesForMenu {

    private final String title;
    private final List<AccountRecord> allRecords;
    private final NumberFormat nf = NumberFormat.getInstance(new Locale("en", "IN"));

    private final String[] sortOptions = {
            "DPD (high to low)",
            "DPD (low to high)",
            "Name (A-Z)",
            "EMI (high to low)"
    };

    private final String[] filterOptions = {
            "All",
            "0-30",
            "31-60",
            "61-90",
            "90+"
    };

    private int sortIndex = 0;
    private int filterIndex = 0;
    private int page = 1;
    private int selectedIndex = 0;

    private final boolean updateMode;
    private final AccountRepository repo;
    private String statusLine = null;

    CustomerListScreen(String title, List<AccountRecord> records) {
        this.title = title;
        this.allRecords = records != null ? new ArrayList<>(records) : new ArrayList<>();
        this.updateMode = false;
        this.repo = null;
    }

    CustomerListScreen(String title, List<AccountRecord> records, AccountRepository repo) {
        this.title = title;
        this.allRecords = records != null ? new ArrayList<>(records) : new ArrayList<>();
        this.updateMode = true;
        this.repo = repo;
    }

    void show(Terminal terminal) throws IOException {
        while (true) {
            int width = VariablesForMenu.getScreenWidth(terminal);

            int height = terminal.getSize().getRows();
            int fixedLines = updateMode ? 9 : 8;
            int pageSize = height - fixedLines;
            if (pageSize < 5) {
                pageSize = 5;
            }
            if (pageSize > 25) {
                pageSize = 25;
            }

            List<AccountRecord> filtered = getSortedAndFilteredRecords();
            int totalItems = filtered.size();
            int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / pageSize));
            if (page > totalPages) {
                page = totalPages;
            }
            if (page < 1) {
                page = 1;
            }

            int startIndex = (page - 1) * pageSize;
            int endIndex = Math.min(totalItems, startIndex + pageSize);
            int itemsOnPage = Math.max(0, endIndex - startIndex);
            if (selectedIndex >= itemsOnPage && itemsOnPage > 0) {
                selectedIndex = itemsOnPage - 1;
            }
            if (itemsOnPage == 0) {
                selectedIndex = 0;
            }

            draw(terminal, width, pageSize, filtered, totalPages, startIndex, endIndex, itemsOnPage);

            int readKey = terminal.reader().read();
            statusLine = null;

            if (readKey == 27) {
                readKey = terminal.reader().read();
                if (readKey == 79 || readKey == 91) {
                    readKey = terminal.reader().read();
                    if (readKey == 65) {
                        if (itemsOnPage > 0) {
                            selectedIndex = (selectedIndex - 1 + itemsOnPage) % itemsOnPage;
                        }
                    } else if (readKey == 66) {
                        if (itemsOnPage > 0) {
                            selectedIndex = (selectedIndex + 1) % itemsOnPage;
                        }
                    } else if (readKey == 67) {
                        if (totalPages > 1) {
                            page = (page % totalPages) + 1;
                            selectedIndex = 0;
                        }
                    } else if (readKey == 68) {
                        if (totalPages > 1) {
                            page = (page == 1) ? totalPages : page - 1;
                            selectedIndex = 0;
                        }
                    }
                }
            } else if (readKey == 'S' || readKey == 's') {
                sortIndex = (sortIndex + 1) % sortOptions.length;
                page = 1;
                selectedIndex = 0;
            } else if (readKey == 'F' || readKey == 'f') {
                filterIndex = (filterIndex + 1) % filterOptions.length;
                page = 1;
                selectedIndex = 0;
            } else if (readKey == 13 || readKey == 10) {
                if (itemsOnPage > 0) {
                    AccountRecord rec = filtered.get(startIndex + selectedIndex);
                    if (updateMode) {
                        String savedAcc = new AccountEditScreen(rec, repo).show(terminal);
                        if (savedAcc != null) {
                            try {
                                List<AccountRecord> reloaded = repo.findAll();
                                allRecords.clear();
                                allRecords.addAll(reloaded);
                            } catch (DataAccessException e) {
                                statusLine = RED + e.getMessage() + RESET;
                            }

                            List<AccountRecord> curFiltered = getSortedAndFilteredRecords();
                            int found = -1;
                            for (int i = 0; i < curFiltered.size(); i++) {
                                if (curFiltered.get(i).getAccount().getAccountNumber().equals(savedAcc)) {
                                    found = i;
                                    break;
                                }
                            }

                            if (found == -1) {
                                filterIndex = 0;
                                curFiltered = getSortedAndFilteredRecords();
                                for (int i = 0; i < curFiltered.size(); i++) {
                                    if (curFiltered.get(i).getAccount().getAccountNumber().equals(savedAcc)) {
                                        found = i;
                                        break;
                                    }
                                }
                            }

                            if (found != -1) {
                                page = (found / pageSize) + 1;
                                selectedIndex = found % pageSize;
                            }

                            statusLine = GREEN + "Updated account " + savedAcc + RESET;
                        }
                    } else {
                        new AccountDetailScreen(rec).show(terminal);
                    }
                }
            } else if (readKey == 'P' || readKey == 'p') {
                if (updateMode && itemsOnPage > 0) {
                    AccountRecord rec = filtered.get(startIndex + selectedIndex);
                    LoanAccount acc = rec.getAccount();
                    if (acc.getEmisMissed() == 0) {
                        statusLine = YELLOW + "Account " + acc.getAccountNumber() + " is already PAID" + RESET;
                    } else {
                        Customer cust = rec.getCustomer();
                        LoanAccount paidAcc = LoanAccount.fromCode(
                                acc.getLoanTypeCode(), acc.getAccountNumber(), cust.getUniqueId(),
                                acc.getOpenDate(), (int) acc.getLoanAmount(), (int) acc.getEmiAmount(),
                                acc.getDueDay(), 0);
                        paidAcc.calculateDpd(LocalDate.now());
                        try {
                            repo.updateRecord(cust, paidAcc);
                            List<AccountRecord> reloaded = repo.findAll();
                            allRecords.clear();
                            allRecords.addAll(reloaded);
                            statusLine = GREEN + "Marked account " + acc.getAccountNumber() + " as PAID" + RESET;
                        } catch (AccountNotFoundException | DataAccessException e) {
                            statusLine = RED + e.getMessage() + RESET;
                        }
                    }
                }
            } else if (readKey == 'B' || readKey == 'b') {
                return;
            }
        }
    }

    private List<AccountRecord> getSortedAndFilteredRecords() {
        String filter = filterOptions[filterIndex];
        List<AccountRecord> list = new ArrayList<>();
        for (AccountRecord r : allRecords) {
            if ("All".equals(filter) || filter.equals(r.getAccount().getBucket())) {
                list.add(r);
            }
        }

        Comparator<AccountRecord> comp = switch (sortIndex) {
            case 0 -> Comparator.comparingInt((AccountRecord r) -> r.getAccount().getDpd()).reversed();
            case 1 -> Comparator.comparingInt((AccountRecord r) -> r.getAccount().getDpd());
            case 2 -> Comparator.comparing((AccountRecord r) -> r.getCustomer().getName(), String.CASE_INSENSITIVE_ORDER);
            case 3 -> Comparator.comparingDouble((AccountRecord r) -> r.getAccount().getEmiAmount()).reversed();
            default -> (r1, r2) -> 0;
        };
        comp = comp.thenComparing(r -> r.getAccount().getAccountNumber());
        list.sort(comp);
        return list;
    }

    private void draw(Terminal terminal, int width, int pageSize, List<AccountRecord> filtered,
                      int totalPages, int startIndex, int endIndex, int itemsOnPage) {
        StringBuilder sb = new StringBuilder();
        String dashedLine = "-".repeat(width);

        sb.append(HIDECURSOR).append(MOVECURSOR);
        sb.append(ERASELINE).append(dashedLine).append(NEWLINE);

        int titlePad = Math.max(0, (width - title.length()) / 2);
        sb.append(ERASELINE).append(" ".repeat(titlePad)).append(title).append(NEWLINE);

        String info = "Page " + page + "/" + totalPages + "   Sort: " + sortOptions[sortIndex]
                + "   Filter: " + filterOptions[filterIndex] + "   Showing " + filtered.size() + " of " + allRecords.size();
        if (info.length() > width) {
            info = info.substring(0, width);
        }
        sb.append(ERASELINE).append(info).append(NEWLINE);
        sb.append(ERASELINE).append(dashedLine).append(NEWLINE);

        int accWidth = 10;
        for (AccountRecord r : allRecords) {
            int len = r.getAccount().getAccountNumber().length();
            if (len > accWidth) {
                accWidth = len;
            }
        }

        boolean showPhone = width >= 120;
        int typeWidth = 4;
        int emiWidth = 10;
        int missedWidth = 6;
        int dpdWidth = 5;
        int bucketWidth = 6;
        int statusWidth = 6;
        int phoneWidth = showPhone ? 15 : 0;

        int fixedWidth = 2 + accWidth + 1 + 1 + (showPhone ? phoneWidth + 1 : 0)
                + typeWidth + 1 + emiWidth + 1 + missedWidth + 1 + dpdWidth + 1 + bucketWidth + 1 + statusWidth;
        int custWidth = width - fixedWidth;
        if (custWidth < 10) {
            custWidth = 10;
        }

        StringBuilder headerSb = new StringBuilder();
        headerSb.append("  ");
        headerSb.append(padRight("Account No", accWidth)).append(" ");
        headerSb.append(padRight("Customer", custWidth)).append(" ");
        if (showPhone) {
            headerSb.append(padRight("Phone", phoneWidth)).append(" ");
        }
        headerSb.append(padRight("Type", typeWidth)).append(" ");
        headerSb.append(padLeft("EMI", emiWidth)).append(" ");
        headerSb.append(padLeft("Missed", missedWidth)).append(" ");
        headerSb.append(padLeft("DPD", dpdWidth)).append(" ");
        headerSb.append(padRight("Bucket", bucketWidth)).append(" ");
        headerSb.append(padRight("Status", statusWidth));

        String headerLine = headerSb.toString();
        if (headerLine.length() > width) {
            headerLine = headerLine.substring(0, width);
        }
        sb.append(ERASELINE).append(headerLine).append(NEWLINE);
        sb.append(ERASELINE).append(dashedLine).append(NEWLINE);

        if (itemsOnPage == 0) {
            String emptyMsg = "No accounts to show";
            int emptyPad = Math.max(0, (width - emptyMsg.length()) / 2);
            sb.append(ERASELINE).append(" ".repeat(emptyPad)).append(emptyMsg).append(NEWLINE);
            for (int i = 1; i < pageSize; i++) {
                sb.append(ERASELINE).append(NEWLINE);
            }
        } else {
            for (int i = 0; i < pageSize; i++) {
                if (i < itemsOnPage) {
                    AccountRecord rec = filtered.get(startIndex + i);
                    boolean isSelected = (i == selectedIndex);
                    String rowStr = formatRow(rec, isSelected, accWidth, custWidth, showPhone,
                            phoneWidth, typeWidth, emiWidth, missedWidth, dpdWidth, bucketWidth, statusWidth, width);
                    sb.append(ERASELINE).append(rowStr).append(NEWLINE);
                } else {
                    sb.append(ERASELINE).append(NEWLINE);
                }
            }
        }

        if (updateMode) {
            if (statusLine != null) {
                sb.append(ERASELINE).append(statusLine).append(NEWLINE);
            } else {
                sb.append(ERASELINE).append(NEWLINE);
            }
        }

        sb.append(ERASELINE).append(dashedLine).append(NEWLINE);
        String actionKey = updateMode ? "Enter: edit   P: paid" : "Enter: open";
        String help = "Up/Down: select   Left/Right: page   S: sort   F: filter   " + actionKey + "   B: back";
        if (help.length() > width) {
            help = help.substring(0, width);
        }
        sb.append(ERASELINE).append(help).append(NEWLINE);
        sb.append(ERASEBELOW);

        System.out.print(sb);
        System.out.flush();
    }

    private String formatRow(AccountRecord rec, boolean isSelected, int accWidth, int custWidth,
                             boolean showPhone, int phoneWidth, int typeWidth, int emiWidth,
                             int missedWidth, int dpdWidth, int bucketWidth, int statusWidth, int screenWidth) {
        Customer cust = rec.getCustomer();
        LoanAccount acc = rec.getAccount();

        String accStr = padRight(acc.getAccountNumber(), accWidth);

        String name = cust.getName();
        if (name.length() > custWidth) {
            name = name.substring(0, custWidth - 3) + "...";
        }
        String custStr = padRight(name, custWidth);

        String phoneStr = "";
        if (showPhone) {
            String phone = cust.getPhone();
            if (phone == null || phone.isEmpty()) {
                phone = "-";
            }
            if (phone.length() > phoneWidth) {
                phone = phone.substring(0, phoneWidth);
            }
            phoneStr = padRight(phone, phoneWidth);
        }

        String typeStr = padRight(acc.getLoanTypeCode(), typeWidth);
        String emiStr = padLeft(nf.format((long) acc.getEmiAmount()), emiWidth);
        String missedStr = padLeft(String.valueOf(acc.getEmisMissed()), missedWidth);
        String dpdStr = padLeft(String.valueOf(acc.getDpd()), dpdWidth);

        String bucketPadded = padRight(acc.getBucket(), bucketWidth);
        String bucketColor = VariablesForMenu.getBucketColor(acc.getBucket());
        String bucketColored = bucketColor + bucketPadded + RESET + (isSelected ? REVERSE : "");

        String statusPadded = padRight(acc.getPaidStatus(), statusWidth);
        String statusColor = VariablesForMenu.getStatusColor(acc.getPaidStatus());
        String statusColored = statusColor + statusPadded + RESET + (isSelected ? REVERSE : "");

        StringBuilder row = new StringBuilder();
        if (isSelected) {
            row.append(REVERSE);
            row.append("> ");
        } else {
            row.append("  ");
        }

        row.append(accStr).append(" ");
        row.append(custStr).append(" ");
        if (showPhone) {
            row.append(phoneStr).append(" ");
        }
        row.append(typeStr).append(" ");
        row.append(emiStr).append(" ");
        row.append(missedStr).append(" ");
        row.append(dpdStr).append(" ");
        row.append(bucketColored).append(" ");
        row.append(statusColored);

        if (isSelected) {
            row.append(RESET);
        }

        return row.toString();
    }

    private String padRight(String s, int n) {
        if (s == null) {
            s = "";
        }
        if (s.length() >= n) {
            return s.substring(0, n);
        }
        return s + " ".repeat(n - s.length());
    }

    private String padLeft(String s, int n) {
        if (s == null) {
            s = "";
        }
        if (s.length() >= n) {
            return s.substring(0, n);
        }
        return " ".repeat(n - s.length()) + s;
    }
}
