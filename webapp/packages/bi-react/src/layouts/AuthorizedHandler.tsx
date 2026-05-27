import { AuthorizedUser, getAuthorizedUser } from "@/services/auth";
import { clearAccount, setPermissions, setRoles, setTenant, setTenants, setUser } from "@/store/accountSlice";
import { getTenantId, getToken } from "@/utils/token";
import { useQuery } from "@tanstack/react-query";
import { useEffect } from "react";
import { useDispatch } from "react-redux";
import { useNavigate } from "react-router-dom";

const AuthorizedHandler: React.FC<{ redirect?: boolean }> = ({
  redirect = false,
}) => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const token = getToken();
  const { data: user } = useQuery<AuthorizedUser | null>({
    queryKey: ["authorizedUser", token],
    queryFn: () =>
      getAuthorizedUser().then((res) => {
        if (res.code === "OK") {
          return res.data;
        }
        clearAccount();
        if (redirect) {
          navigate("/login");
        }
        return null;
      }),
    enabled: !!token,
  });

  useEffect(() => {
    if (!token) {
      clearAccount();
      redirect && navigate("/login");
    }
  }, [token, redirect]);

  useEffect(() => {
    if (user) {
      const tenants = user.tenants || [];
      const tenantId = getTenantId();
      if (tenantId) {
        const tenant = tenants.find((t) => t.id === tenantId);
        if (tenant) {
          dispatch(setTenant(tenant));
        }
      }
      dispatch(setTenants(tenants));
      dispatch(setUser(user));
      dispatch(setPermissions(user.permissions || []));
      dispatch(setRoles(user.roles || []));
    }
  }, [user, dispatch, redirect]);
  return <></>;
};

export default AuthorizedHandler;
