namespace LOC.Core.Data
{
    using System;
    using System.Data.Entity;
    using System.Linq;
    using System.Linq.Expressions;

    public interface IRepository : IDisposable
    {
        TEntity Add<TEntity>(TEntity entity) where TEntity : class;
        bool Any<TEntity>(Expression<Func<TEntity, bool>> predicate) where TEntity : class;
        bool Any<TEntity>() where TEntity : class;

        void CommitChanges();
        void Delete<TEntity>(TEntity entity) where TEntity : class;
        void Edit<TEntity>(TEntity entity) where TEntity : class;
        void Attach<TEntity>(TEntity entity) where TEntity : class;

        IQueryable<TEntity> GetAll<TEntity>() where TEntity : class;

        TEntity GetByKeyValues<TEntity>(params object[] keyValues) where TEntity : class;
        IQueryable<TEntity> Where<TEntity>(Expression<Func<TEntity, bool>> predicate) where TEntity : class;
        DbContext Context { get; }
    }
}
