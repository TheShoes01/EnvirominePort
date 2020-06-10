package envirominePort.utils;

import envirominePort.client.gui.hud.HUDRegistry;

public enum Alignment {
    TOPLEFT, TOPCENTER, TOPRIGHT, CENTERLEFT, CENTERCENTER, CENTERRIGHT, BOTTOMLEFT, BOTTOMCENTER, BOTTOMRIGHT;

    public static Alignment fromString(String string) {
        switch (string) {
            case "TOPLEFT":
                return TOPLEFT;
            case "TOPCENTER":
                return TOPCENTER;
            case "TOPRIGHT":
                return TOPRIGHT;
            case "CENTERLEFT":
                return CENTERLEFT;
            case "CENTERCENTER":
                return CENTERCENTER;
            case "CENTERRIGHT":
                return CENTERRIGHT;
            case "BOTTOMLEFT":
                return BOTTOMLEFT;
            case "BOTTOMCENTER":
                return BOTTOMCENTER;
            case "BOTTOMRIGHT":
                return BOTTOMRIGHT;
            default:
                return CENTERCENTER;
        }
    }

    public static Alignment calculateAlignment(int x, int y) {
        return calculateAlignment(x, y, HUDRegistry.screenWidth, HUDRegistry.screenHeight);
    }

    public static Alignment calculateAlignment(int x, int y, int screenWidth, int screenHeight) {
        x = (int) (3.0F / screenWidth * x);
        y = (int) (3.0F / screenHeight * y);

        if (x == 0 && y == 0) {
            return TOPLEFT;
        }
        if (x == 1 && y == 0) {
            return TOPCENTER;
        }
        if (x == 2 && y == 0) {
            return TOPRIGHT;
        }
        if (x == 0 && y == 1) {
            return CENTERLEFT;
        }
        if (x == 1 && y == 1) {
            return CENTERCENTER;
        }
        if (x == 2 && y == 1) {
            return CENTERRIGHT;
        }
        if (x == 0 && y == 2) {
            return BOTTOMLEFT;
        }
        if (x == 1 && y == 2) {
            return BOTTOMCENTER;
        }
        if (x == 2 && y == 2) {
            return BOTTOMRIGHT;
        }

        return CENTERCENTER;
    }

    public static boolean isTop(Alignment alignment) {
        switch (alignment) {
            case TOPLEFT:
            case TOPCENTER:
            case TOPRIGHT:
                return true;
            default:
                return false;
        }
    }

    public static boolean isVerticalCenter(Alignment alignment) {
        switch (alignment) {
            case CENTERLEFT:
            case CENTERCENTER:
            case CENTERRIGHT:
                return true;
            default:
                return false;
        }
    }

    public static boolean isBottom(Alignment alignment) {
        switch (alignment) {
            case BOTTOMLEFT:
            case BOTTOMCENTER:
            case BOTTOMRIGHT:
                return true;
            default:
                return false;
        }
    }

    public static boolean isLeft(Alignment alignment) {
        switch (alignment) {
            case TOPLEFT:
            case CENTERLEFT:
            case BOTTOMLEFT:
                return true;
            default:
                return false;
        }
    }


    public static boolean isHorizontalCenter(Alignment alignment) {
        switch (alignment) {
            case TOPCENTER:
            case CENTERCENTER:
            case BOTTOMCENTER:
                return true;
            default:
                return false;
        }
    }

    public static boolean isRight(Alignment alignment) {
        switch (alignment) {
            case TOPRIGHT:
            case CENTERRIGHT:
            case BOTTOMRIGHT:
                return true;
            default:
                return false;
        }
    }
}
